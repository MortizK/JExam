# Projektmetriken

Dieses Dokument sammelt reproduzierbare Messungen aus dem aktuellen Quellstand von JExam.

## Messung vom 2026-05-15T14:43:03+02:00

### Direkte Projektmetriken

| Metrik | Wert |
|---|---:|
| Java-Dateien gesamt | 79 |
| Java-Dateien in `src/main/java` | 56 |
| Java-Dateien in `src/test/java` | 23 |
| Pakete gesamt | 14 |
| Pakete in `src/main/java` | 12 |
| Pakete in `src/test/java` | 9 |
| Klassen/Interfaces/Enums/Records | 24 |
| Methoden/Konstruktoren | 658 |
| Physische Zeilen gesamt | 12895 |
| Code-Zeilen | 8240 |
| Kommentarzeilen | 3183 |
| Leerzeilen | 1472 |
| Kommentaranteil | 24.68 % |
| Code-Dichte | 63.9 % |
| Durchschnittliche Methodengröße | 12.52 Code-Zeilen |
| Geschätzte zyklomatische Komplexität gesamt | 1384 |
| Geschätzte zyklomatische Komplexität je Methode | 2.1 |
| Durchschnittliche Klassen pro Paket | 1.71 |
| Durchschnittliche Methoden pro Klasse | 27.42 |

### Kopplung

- Efferente Kopplung (interne Paketabhängigkeiten): 35
- Afferente Kopplung je Paket: siehe JSON-Block in dieser Datei.
- Die Kopplungswerte werden aus internen `import`-Anweisungen abgeleitet.

### Kohäsion

- Kohäsion wird hier nicht direkt als IEEE-/Metrics-Tool-Wert berechnet.
- Für eine strengere Messung eignet sich ein statisches Analysewerkzeug wie CodeMR, SonarQube oder eine IDE-Erweiterung mit LCOM-/Cohesion-Unterstützung.

### Rohdaten

```json
{
  "generated_at": "2026-05-15T14:43:03+02:00",
  "java_files": 79,
  "main_java_files": 56,
  "test_java_files": 23,
  "packages": 14,
  "main_packages": 12,
  "test_packages": 9,
  "classes": 24,
  "methods": 658,
  "physical_lines": 12895,
  "code_lines": 8240,
  "comment_lines": 3183,
  "blank_lines": 1472,
  "comment_share_percent": 24.68,
  "code_density_percent": 63.9,
  "average_method_length_code_lines": 12.52,
  "estimated_cyclomatic_total": 1384,
  "estimated_cyclomatic_average": 2.1,
  "internal_dependency_edges": 35,
  "package_coupling_out": {
    "com.jexam": 2,
    "com.jexam.app": 10,
    "com.jexam.app.ui": 0,
    "com.jexam.app.ui.base": 0,
    "com.jexam.app.ui.components": 1,
    "com.jexam.app.ui.components.pdf": 5,
    "com.jexam.app.ui.components.xml": 1,
    "com.jexam.app.ui.styling": 0,
    "com.jexam.generation": 3,
    "com.jexam.io": 4,
    "com.jexam.model": 1,
    "com.jexam.model.enums": 0,
    "com.jexam.validation": 3,
    "com.jexam.workflow": 5
  },
  "package_coupling_in": {
    "com.jexam": 2,
    "com.jexam.app": 3,
    "com.jexam.app.ui": 1,
    "com.jexam.app.ui.base": 0,
    "com.jexam.app.ui.components": 1,
    "com.jexam.app.ui.components.pdf": 1,
    "com.jexam.app.ui.components.xml": 1,
    "com.jexam.app.ui.styling": 1,
    "com.jexam.generation": 3,
    "com.jexam.io": 2,
    "com.jexam.model": 7,
    "com.jexam.model.enums": 9,
    "com.jexam.validation": 4,
    "com.jexam.workflow": 0
  },
  "avg_classes_per_package": 1.71,
  "avg_methods_per_class": 27.42,
  "package_sizes": {
    "com.jexam.app": 19,
    "com.jexam.app.ui.base": 2,
    "com.jexam.app.ui.components": 5,
    "com.jexam.app.ui.components.pdf": 4,
    "com.jexam.app.ui.components.xml": 7,
    "com.jexam.app.ui.styling": 3,
    "com.jexam.app.ui": 2,
    "com.jexam.generation": 6,
    "com.jexam.io": 8,
    "com.jexam.model": 6,
    "com.jexam.model.enums": 3,
    "com.jexam.validation": 5,
    "com.jexam": 1,
    "com.jexam.workflow": 3
  },
  "top_packages_by_size": [
    [
      "com.jexam.app",
      19
    ],
    [
      "com.jexam.io",
      8
    ],
    [
      "com.jexam.app.ui.components.xml",
      7
    ],
    [
      "com.jexam.generation",
      6
    ],
    [
      "com.jexam.model",
      6
    ],
    [
      "com.jexam.app.ui.components",
      5
    ],
    [
      "com.jexam.validation",
      5
    ],
    [
      "com.jexam.app.ui.components.pdf",
      4
    ],
    [
      "com.jexam.app.ui.styling",
      3
    ],
    [
      "com.jexam.model.enums",
      3
    ]
  ]
}
```
