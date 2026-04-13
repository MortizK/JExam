from __future__ import annotations

import json
from pathlib import Path

import fitz


def round2(v: float) -> float:
    return round(float(v), 2)


def bbox_dict(b):
    return {"x0": round2(b[0]), "y0": round2(b[1]), "x1": round2(b[2]), "y1": round2(b[3])}


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    pdf_path = root / "Exams" / "WE1_Probeklausur.pdf"
    if not pdf_path.exists():
        raise FileNotFoundError(pdf_path)

    doc = fitz.open(pdf_path)
    page = doc[0]

    page_size = {"width_pt": round2(page.rect.width), "height_pt": round2(page.rect.height)}

    text_dict = page.get_text("dict")
    raw_dict = page.get_text("rawdict")

    spans = []
    for block in text_dict.get("blocks", []):
        for line in block.get("lines", []):
            for span in line.get("spans", []):
                text = (span.get("text") or "").strip()
                if not text:
                    continue
                spans.append(
                    {
                        "text": text,
                        "size": round2(span.get("size", 0.0)),
                        "font": span.get("font", ""),
                        "bbox": bbox_dict(span.get("bbox")),
                    }
                )

    spans_sorted = sorted(spans, key=lambda s: (s["bbox"]["y0"], s["bbox"]["x0"]))

    matrikel = [s for s in spans_sorted if "matrikel" in s["text"].lower()]
    title = [s for s in spans_sorted if "probeklausur" in s["text"].lower()]

    # Search for square-like characters around the matrikel line.
    square_chars = []
    for block in raw_dict.get("blocks", []):
        for line in block.get("lines", []):
            for span in line.get("spans", []):
                for ch in span.get("chars", []):
                    c = ch.get("c", "")
                    if c in {"□", "◻", "◽", "■", "▢"}:
                        square_chars.append({"char": c, "bbox": bbox_dict(ch.get("bbox"))})

    drawings = page.get_drawings()
    rects = []
    lines = []
    for d in drawings:
        for item in d.get("items", []):
            if not item:
                continue
            if item[0] == "re":
                r = item[1]
                rects.append(
                    {
                        "x0": round2(r.x0),
                        "y0": round2(r.y0),
                        "x1": round2(r.x1),
                        "y1": round2(r.y1),
                        "w": round2(r.width),
                        "h": round2(r.height),
                    }
                )
            elif item[0] == "l":
                p1 = item[1]
                p2 = item[2]
                lines.append(
                    {
                        "x0": round2(p1.x),
                        "y0": round2(p1.y),
                        "x1": round2(p2.x),
                        "y1": round2(p2.y),
                        "horizontal": round2(p1.y) == round2(p2.y),
                        "vertical": round2(p1.x) == round2(p2.x),
                        "length": round2(((p2.x - p1.x) ** 2 + (p2.y - p1.y) ** 2) ** 0.5),
                    }
                )

    square_rects = [r for r in rects if abs(r["w"] - r["h"]) <= 1.5]

    # Nearby square rects around matrikel line if available.
    nearby_square_rects = []
    if matrikel:
        y = matrikel[0]["bbox"]["y0"]
        nearby_square_rects = [r for r in square_rects if abs(r["y0"] - y) <= 30 or abs(r["y1"] - y) <= 30]

    long_h_lines = [l for l in lines if l["horizontal"] and l["length"] >= 80]
    long_v_lines = [l for l in lines if l["vertical"] and l["length"] >= 80]

    result = {
        "file": pdf_path.name,
        "page": 1,
        "page_size": page_size,
        "matrikel_spans": matrikel,
        "title_spans": title,
        "matrikel_above_title": bool(matrikel and title and matrikel[0]["bbox"]["y0"] < title[0]["bbox"]["y0"]),
        "square_char_count": len(square_chars),
        "square_chars": square_chars,
        "draw_rect_count": len(rects),
        "draw_line_count": len(lines),
        "draw_lines": lines,
        "square_rect_count": len(square_rects),
        "nearby_square_rect_count": len(nearby_square_rects),
        "nearby_square_rects": nearby_square_rects,
        "long_horizontal_lines": long_h_lines,
        "long_vertical_lines": long_v_lines,
        "top_spans_by_y": spans_sorted[:40],
    }

    out = root / "testing" / "we1_cover_analysis.json"
    out.write_text(json.dumps(result, indent=2, ensure_ascii=True), encoding="utf-8")

    print(f"Wrote {out}")
    print(f"Matrikel span count: {len(matrikel)}")
    print(f"Title span count: {len(title)}")
    print(f"Matrikel above title: {result['matrikel_above_title']}")
    print(f"Square chars on page: {len(square_chars)}")
    print(f"Draw rects on page: {len(rects)}")
    print(f"Draw lines on page: {len(lines)}")
    print(f"Square rects on page: {len(square_rects)}")
    print(f"Nearby square rects: {len(nearby_square_rects)}")

    doc.close()


if __name__ == "__main__":
    main()
