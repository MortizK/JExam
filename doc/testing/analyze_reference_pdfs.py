from __future__ import annotations

import json
import re
import statistics as stats
from dataclasses import dataclass, asdict
from pathlib import Path

import fitz  # PyMuPDF


HEADING_PATTERNS = [
    re.compile(r"^Aufgabe\s+\d+", re.IGNORECASE),
    re.compile(r"^[a-z]\)", re.IGNORECASE),
    re.compile(r"^Lösung", re.IGNORECASE),
    re.compile(r"^Probeklausur", re.IGNORECASE),
]


@dataclass
class PageMetrics:
    page_index: int
    width_pt: float
    height_pt: float
    margin_left_pt: float | None
    margin_right_pt: float | None
    margin_top_pt: float | None
    margin_bottom_pt: float | None
    median_line_height_pt: float | None
    median_font_size_pt: float | None
    text_blocks: int
    drawing_rects: int


@dataclass
class PdfMetrics:
    file: str
    pages: int
    page_size_pt: tuple[float, float] | None
    margin_summary_pt: dict[str, float | None]
    median_line_height_pt: float | None
    median_font_size_pt: float | None
    common_font_sizes_pt: list[tuple[float, int]]
    heading_matches: dict[str, int]
    drawing_rects_total: int
    per_page: list[PageMetrics]


def round2(value: float | None) -> float | None:
    if value is None:
        return None
    return round(float(value), 2)


def safe_median(values: list[float]) -> float | None:
    return round2(stats.median(values)) if values else None


def top_counts(values: list[float], top_n: int = 6) -> list[tuple[float, int]]:
    counts: dict[float, int] = {}
    for v in values:
        key = round(v, 1)
        counts[key] = counts.get(key, 0) + 1
    items = sorted(counts.items(), key=lambda kv: kv[1], reverse=True)
    return [(round2(k), v) for k, v in items[:top_n]]


def line_height_from_span(span: dict) -> float | None:
    bbox = span.get("bbox")
    if not bbox or len(bbox) != 4:
        return None
    y0, y1 = bbox[1], bbox[3]
    return float(y1 - y0) if y1 > y0 else None


def analyze_pdf(path: Path) -> PdfMetrics:
    doc = fitz.open(path)

    all_left: list[float] = []
    all_right: list[float] = []
    all_top: list[float] = []
    all_bottom: list[float] = []
    all_line_heights: list[float] = []
    all_font_sizes: list[float] = []
    heading_counts = {p.pattern: 0 for p in HEADING_PATTERNS}
    per_page: list[PageMetrics] = []
    total_rects = 0

    first_page_size = None

    for i, page in enumerate(doc):
        page_rect = page.rect
        if first_page_size is None:
            first_page_size = (round2(page_rect.width), round2(page_rect.height))

        text_dict = page.get_text("dict")
        words = page.get_text("words")

        left_margin = right_margin = top_margin = bottom_margin = None
        if words:
            x0s = [w[0] for w in words]
            y0s = [w[1] for w in words]
            x1s = [w[2] for w in words]
            y1s = [w[3] for w in words]
            left_margin = min(x0s)
            top_margin = min(y0s)
            right_margin = page_rect.width - max(x1s)
            bottom_margin = page_rect.height - max(y1s)

            all_left.append(left_margin)
            all_top.append(top_margin)
            all_right.append(right_margin)
            all_bottom.append(bottom_margin)

        page_line_heights: list[float] = []
        for block in text_dict.get("blocks", []):
            for line in block.get("lines", []):
                for span in line.get("spans", []):
                    size = span.get("size")
                    if size:
                        all_font_sizes.append(float(size))
                    h = line_height_from_span(span)
                    if h:
                        all_line_heights.append(h)
                        page_line_heights.append(h)

                    text = (span.get("text") or "").strip()
                    if text:
                        for pat in HEADING_PATTERNS:
                            if pat.match(text):
                                heading_counts[pat.pattern] += 1

        drawings = page.get_drawings()
        rect_count = 0
        for d in drawings:
            for item in d.get("items", []):
                if not item:
                    continue
                op = item[0]
                if op == "re":
                    rect_count += 1

        total_rects += rect_count

        per_page.append(
            PageMetrics(
                page_index=i + 1,
                width_pt=round2(page_rect.width),
                height_pt=round2(page_rect.height),
                margin_left_pt=round2(left_margin),
                margin_right_pt=round2(right_margin),
                margin_top_pt=round2(top_margin),
                margin_bottom_pt=round2(bottom_margin),
                median_line_height_pt=safe_median(page_line_heights),
                median_font_size_pt=safe_median(
                    [s.get("size") for b in text_dict.get("blocks", []) for l in b.get("lines", []) for s in l.get("spans", []) if s.get("size")]
                ),
                text_blocks=len(text_dict.get("blocks", [])),
                drawing_rects=rect_count,
            )
        )

    metrics = PdfMetrics(
        file=path.name,
        pages=doc.page_count,
        page_size_pt=first_page_size,
        margin_summary_pt={
            "left_median": safe_median(all_left),
            "right_median": safe_median(all_right),
            "top_median": safe_median(all_top),
            "bottom_median": safe_median(all_bottom),
        },
        median_line_height_pt=safe_median(all_line_heights),
        median_font_size_pt=safe_median(all_font_sizes),
        common_font_sizes_pt=top_counts(all_font_sizes),
        heading_matches=heading_counts,
        drawing_rects_total=total_rects,
        per_page=per_page,
    )
    doc.close()
    return metrics


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    exams_dir = root / "Exams"
    targets = [
        exams_dir / "TI2_Probeklausur_Lsg.pdf",
        exams_dir / "WE1_Probeklausur.pdf",
    ]

    results: list[PdfMetrics] = []
    for p in targets:
        if not p.exists():
            print(f"[WARN] Missing file: {p}")
            continue
        results.append(analyze_pdf(p))

    output_dir = root / "testing"
    output_dir.mkdir(parents=True, exist_ok=True)
    output_path = output_dir / "pdf_analysis_report.json"
    output_path.write_text(
        json.dumps([asdict(r) for r in results], indent=2, ensure_ascii=True),
        encoding="utf-8",
    )

    print("PDF analysis complete")
    print(f"Report: {output_path}")
    for r in results:
        print("-")
        print(f"File: {r.file}")
        print(f"Pages: {r.pages}")
        print(f"Page size (pt): {r.page_size_pt}")
        print(f"Median margins (pt): {r.margin_summary_pt}")
        print(f"Median line height (pt): {r.median_line_height_pt}")
        print(f"Median font size (pt): {r.median_font_size_pt}")
        print(f"Top font sizes (pt): {r.common_font_sizes_pt}")
        print(f"Drawing rects total: {r.drawing_rects_total}")


if __name__ == "__main__":
    main()
