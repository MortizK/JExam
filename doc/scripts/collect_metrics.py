from __future__ import annotations

import argparse
import json
import re
from collections import Counter, defaultdict
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[2]
SOURCE_ROOTS = [PROJECT_ROOT / "src" / "main" / "java", PROJECT_ROOT / "src" / "test" / "java"]
REPORT_FILE = PROJECT_ROOT / "doc" / "Metriken.md"
PROJECT_PACKAGE_PREFIX = "com.jexam"


TYPE_RE = re.compile(r"^\s*(?:public|protected|private|abstract|final|static\s+)?(?:class|interface|enum|record)\s+(\w+)")
PACKAGE_RE = re.compile(r"^\s*package\s+([\w\.]+)\s*;")
IMPORT_RE = re.compile(r"^\s*import\s+(?:static\s+)?([\w\.]+)\s*;")
BRANCH_RE = re.compile(r"\b(if|for|while|case|catch|switch)\b|&&|\|\||\?")


@dataclass
class FileMetrics:
    path: Path
    package: str
    code_lines: int
    comment_lines: int
    blank_lines: int
    physical_lines: int
    class_count: int
    method_count: int
    estimated_cyclomatic: int
    imports: list[str]


def strip_to_code(line: str, in_block_comment: bool) -> tuple[str, bool]:
    result: list[str] = []
    index = 0
    in_string: str | None = None

    while index < len(line):
        current = line[index]
        next_char = line[index + 1] if index + 1 < len(line) else ""

        if in_block_comment:
            if current == "*" and next_char == "/":
                in_block_comment = False
                index += 2
            else:
                index += 1
            continue

        if in_string is not None:
            if current == "\\":
                index += 2
                continue
            if current == in_string:
                in_string = None
            index += 1
            continue

        if current == "/" and next_char == "/":
            break
        if current == "/" and next_char == "*":
            in_block_comment = True
            index += 2
            continue
        if current in {'"', "'"}:
            in_string = current
            result.append(" ")
            index += 1
            continue

        result.append(current)
        index += 1

    return "".join(result), in_block_comment


def collect_java_files() -> list[Path]:
    files: list[Path] = []
    for source_root in SOURCE_ROOTS:
        if source_root.exists():
            files.extend(sorted(source_root.rglob("*.java")))
    return files


def package_name_for_path(path: Path) -> str:
    try:
        relative = path.relative_to(PROJECT_ROOT / "src")
    except ValueError:
        return ""
    package_parts = relative.parts[2:-1]
    return ".".join(package_parts)


def measure_file(path: Path) -> FileMetrics:
    lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
    package_name = ""
    imports: list[str] = []

    code_lines = 0
    comment_lines = 0
    blank_lines = 0
    class_count = 0
    method_count = 0
    estimated_cyclomatic = 0

    in_block_comment = False
    clean_lines: list[str] = []
    depth = 0
    package_seen = False

    def is_method_candidate(line: str) -> bool:
        stripped = line.strip()
        if not stripped or stripped.startswith("@"):
            return False
        if stripped.endswith(";") or stripped.endswith(","):
            return False
        if stripped.startswith(("if ", "for ", "while ", "switch ", "catch ", "else ", "do ", "try ", "return ", "new ")):
            return False
        if "(" not in stripped or ")" not in stripped or "{" not in stripped:
            return False
        if any(token in stripped for token in (" class ", " interface ", " enum ", " record ")):
            return False
        leading_tokens = ("public ", "protected ", "private ", "static ", "final ", "abstract ", "synchronized ", "default ")
        return stripped.startswith(leading_tokens) or stripped[:1].isalpha()

    for line in lines:
        stripped_original = line.strip()
        if not stripped_original:
            blank_lines += 1

        clean_line, in_block_comment = strip_to_code(line, in_block_comment)
        clean_stripped = clean_line.strip()
        clean_lines.append(clean_line)

        if stripped_original and not clean_stripped:
            comment_lines += 1
        elif clean_stripped:
            code_lines += 1

        if not package_seen:
            match = PACKAGE_RE.match(clean_line)
            if match:
                package_name = match.group(1)
            package_seen = True

        import_match = IMPORT_RE.match(clean_line)
        if import_match:
            imports.append(import_match.group(1))

        if depth == 0 and TYPE_RE.match(clean_line):
            class_count += 1

        depth += clean_line.count("{") - clean_line.count("}")

    for clean_line in clean_lines:
        stripped = clean_line.strip()
        if not stripped:
            continue
        estimated_cyclomatic += len(BRANCH_RE.findall(clean_line))
        if is_method_candidate(clean_line):
            method_count += 1
            estimated_cyclomatic += 1

    physical_lines = len(lines)
    return FileMetrics(
        path=path,
        package=package_name,
        code_lines=code_lines,
        comment_lines=comment_lines,
        blank_lines=blank_lines,
        physical_lines=physical_lines,
        class_count=class_count,
        method_count=method_count,
        estimated_cyclomatic=estimated_cyclomatic,
        imports=imports,
    )


def collect_metrics() -> dict[str, object]:
    java_files = collect_java_files()
    file_metrics = [measure_file(path) for path in java_files]

    package_names = sorted({metric.package for metric in file_metrics if metric.package})
    main_metrics = [metric for metric in file_metrics if "/src/main/java/" in metric.path.as_posix()]
    test_metrics = [metric for metric in file_metrics if "/src/test/java/" in metric.path.as_posix()]

    imports_by_package: dict[str, set[str]] = defaultdict(set)
    incoming_edges: dict[str, set[str]] = defaultdict(set)
    package_set = set(package_names)

    for metric in file_metrics:
        if not metric.package:
            continue
        for imported in metric.imports:
            if not imported.startswith(PROJECT_PACKAGE_PREFIX):
                continue
            imported_package = ".".join(imported.split(".")[:-1])
            if not imported_package or imported_package == metric.package:
                continue
            imports_by_package[metric.package].add(imported_package)
            if imported_package in package_set:
                incoming_edges[imported_package].add(metric.package)

    total_physical_lines = sum(metric.physical_lines for metric in file_metrics)
    total_code_lines = sum(metric.code_lines for metric in file_metrics)
    total_comment_lines = sum(metric.comment_lines for metric in file_metrics)
    total_blank_lines = sum(metric.blank_lines for metric in file_metrics)
    total_classes = sum(metric.class_count for metric in file_metrics)
    total_methods = sum(metric.method_count for metric in file_metrics)
    total_cyclomatic = sum(metric.estimated_cyclomatic for metric in file_metrics)

    internal_dependency_edges = sum(len(dependencies) for dependencies in imports_by_package.values())
    package_coupling_out = {package: len(imports_by_package.get(package, set())) for package in package_names}
    package_coupling_in = {package: len(incoming_edges.get(package, set())) for package in package_names}

    average_method_length = round(total_code_lines / total_methods, 2) if total_methods else 0.0
    comment_share = round((total_comment_lines / total_physical_lines) * 100, 2) if total_physical_lines else 0.0
    code_density = round((total_code_lines / total_physical_lines) * 100, 2) if total_physical_lines else 0.0
    avg_cyclomatic = round(total_cyclomatic / total_methods, 2) if total_methods else 0.0

    package_sizes = Counter(metric.package for metric in file_metrics if metric.package)
    avg_classes_per_package = round(total_classes / len(package_names), 2) if package_names else 0.0
    avg_methods_per_class = round(total_methods / total_classes, 2) if total_classes else 0.0

    return {
        "generated_at": datetime.now().astimezone().isoformat(timespec="seconds"),
        "java_files": len(java_files),
        "main_java_files": len(main_metrics),
        "test_java_files": len(test_metrics),
        "packages": len(package_names),
        "main_packages": len({metric.package for metric in main_metrics if metric.package}),
        "test_packages": len({metric.package for metric in test_metrics if metric.package}),
        "classes": total_classes,
        "methods": total_methods,
        "physical_lines": total_physical_lines,
        "code_lines": total_code_lines,
        "comment_lines": total_comment_lines,
        "blank_lines": total_blank_lines,
        "comment_share_percent": comment_share,
        "code_density_percent": code_density,
        "average_method_length_code_lines": average_method_length,
        "estimated_cyclomatic_total": total_cyclomatic,
        "estimated_cyclomatic_average": avg_cyclomatic,
        "internal_dependency_edges": internal_dependency_edges,
        "package_coupling_out": package_coupling_out,
        "package_coupling_in": package_coupling_in,
        "avg_classes_per_package": avg_classes_per_package,
        "avg_methods_per_class": avg_methods_per_class,
        "package_sizes": dict(package_sizes),
        "top_packages_by_size": sorted(package_sizes.items(), key=lambda item: (-item[1], item[0]))[:10],
    }


def render_markdown_report(metrics: dict[str, object]) -> str:
    lines = [
        "",
        f"## Messung vom {metrics['generated_at']}",
        "",
        "### Direkte Projektmetriken",
        "",
        "| Metrik | Wert |",
        "|---|---:|",
        f"| Java-Dateien gesamt | {metrics['java_files']} |",
        f"| Java-Dateien in `src/main/java` | {metrics['main_java_files']} |",
        f"| Java-Dateien in `src/test/java` | {metrics['test_java_files']} |",
        f"| Pakete gesamt | {metrics['packages']} |",
        f"| Pakete in `src/main/java` | {metrics['main_packages']} |",
        f"| Pakete in `src/test/java` | {metrics['test_packages']} |",
        f"| Klassen/Interfaces/Enums/Records | {metrics['classes']} |",
        f"| Methoden/Konstruktoren | {metrics['methods']} |",
        f"| Physische Zeilen gesamt | {metrics['physical_lines']} |",
        f"| Code-Zeilen | {metrics['code_lines']} |",
        f"| Kommentarzeilen | {metrics['comment_lines']} |",
        f"| Leerzeilen | {metrics['blank_lines']} |",
        f"| Kommentaranteil | {metrics['comment_share_percent']} % |",
        f"| Code-Dichte | {metrics['code_density_percent']} % |",
        f"| Durchschnittliche Methodengröße | {metrics['average_method_length_code_lines']} Code-Zeilen |",
        f"| Geschätzte zyklomatische Komplexität gesamt | {metrics['estimated_cyclomatic_total']} |",
        f"| Geschätzte zyklomatische Komplexität je Methode | {metrics['estimated_cyclomatic_average']} |",
        f"| Durchschnittliche Klassen pro Paket | {metrics['avg_classes_per_package']} |",
        f"| Durchschnittliche Methoden pro Klasse | {metrics['avg_methods_per_class']} |",
        "",
        "### Kopplung",
        "",
        f"- Efferente Kopplung (interne Paketabhängigkeiten): {metrics['internal_dependency_edges']}",
        "- Afferente Kopplung je Paket: siehe JSON-Block in dieser Datei.",
        "- Die Kopplungswerte werden aus internen `import`-Anweisungen abgeleitet.",
        "",
        "### Kohäsion",
        "",
        "- Kohäsion wird hier nicht direkt als IEEE-/Metrics-Tool-Wert berechnet.",
        "- Für eine strengere Messung eignet sich ein statisches Analysewerkzeug wie CodeMR, SonarQube oder eine IDE-Erweiterung mit LCOM-/Cohesion-Unterstützung.",
        "",
        "### Rohdaten",
        "",
        "```json",
        json.dumps(metrics, indent=2, ensure_ascii=False),
        "```",
        "",
    ]
    return "\n".join(lines)


def append_report(report_text: str) -> None:
    REPORT_FILE.parent.mkdir(parents=True, exist_ok=True)
    if REPORT_FILE.exists():
        existing = REPORT_FILE.read_text(encoding="utf-8")
        if existing and not existing.endswith("\n"):
            existing += "\n"
    else:
        existing = "# Projektmetriken\n\nDieses Dokument sammelt reproduzierbare Messungen aus dem Quellbaum von JExam.\n\n"

    REPORT_FILE.write_text(existing + report_text, encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description="Collect reproducible project metrics for JExam.")
    parser.add_argument("--json", action="store_true", help="Print the collected metrics as JSON.")
    args = parser.parse_args()

    metrics = collect_metrics()
    report_text = render_markdown_report(metrics)

    append_report(report_text)

    if args.json:
        print(json.dumps(metrics, indent=2, ensure_ascii=False))
    else:
        print(report_text)


if __name__ == "__main__":
    main()