# Projektmetriken

Dieses Dokument sammelt reproduzierbare Messungen aus dem aktuellen Quellstand von JExam.

## Messung vom 2026-05-06T13:10:01+02:00

### Direkte Projektmetriken

| Metrik | Wert |
|---|---:|
| Java-Dateien gesamt | 78 |
| Java-Dateien in `src/main/java` | 55 |
| Java-Dateien in `src/test/java` | 23 |
| Pakete gesamt | 14 |
| Pakete in `src/main/java` | 12 |
| Pakete in `src/test/java` | 9 |
| Klassen/Interfaces/Enums/Records | 24 |
| Methoden/Konstruktoren | 633 |
| Physische Zeilen gesamt | 11389 |
| Code-Zeilen | 7867 |
| Kommentarzeilen | 2101 |
| Leerzeilen | 1421 |
| Kommentaranteil | 18.45 % |
| Code-Dichte | 69.08 % |
| Durchschnittliche Methodengröße | 12.43 Code-Zeilen |
| Geschätzte zyklomatische Komplexität gesamt | 1318 |
| Geschätzte zyklomatische Komplexität je Methode | 2.08 |
| Durchschnittliche Klassen pro Paket | 1.71 |
| Durchschnittliche Methoden pro Klasse | 26.38 |

### Kopplung

- Efferente Kopplung (interne Paketabhängigkeiten): 34
- Afferente Kopplung je Paket: siehe JSON-Block in dieser Datei.
- Die Kopplungswerte werden aus internen `import`-Anweisungen abgeleitet.

### Kohäsion

- Kohäsion wird hier nicht direkt als IEEE-/Metrics-Tool-Wert berechnet.
- Für eine strengere Messung eignet sich ein statisches Analysewerkzeug wie CodeMR, SonarQube oder eine IDE-Erweiterung mit LCOM-/Cohesion-Unterstützung.

### Rohdaten

```json
{
  "generated_at": "2026-05-06T13:10:01+02:00",
  "java_files": 78,
  "main_java_files": 55,
  "test_java_files": 23,
  "packages": 14,
  "main_packages": 12,
  "test_packages": 9,
  "classes": 24,
  "methods": 633,
  "physical_lines": 11389,
  "code_lines": 7867,
  "comment_lines": 2101,
  "blank_lines": 1421,
  "comment_share_percent": 18.45,
  "code_density_percent": 69.08,
  "average_method_length_code_lines": 12.43,
  "estimated_cyclomatic_total": 1318,
  "estimated_cyclomatic_average": 2.08,
  "internal_dependency_edges": 34,
  "package_coupling_out": {
    "com.jexam": 2,
    "com.jexam.app": 10,
    "com.jexam.app.ui": 0,
    "com.jexam.app.ui.base": 0,
    "com.jexam.app.ui.components": 1,
    "com.jexam.app.ui.components.pdf": 5,
    "com.jexam.app.ui.components.xml": 1,
    "com.jexam.app.ui.styling": 0,
    "com.jexam.generation": 2,
    "com.jexam.io": 4,
    "com.jexam.model": 1,
    "com.jexam.model.enums": 0,
    "com.jexam.validation": 3,
    "com.jexam.workflow": 5
  },
  "package_coupling_in": {
    "com.jexam": 2,
    "com.jexam.app": 2,
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
  "avg_methods_per_class": 26.38,
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
    "com.jexam.model": 5,
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
      "com.jexam.app.ui.components",
      5
    ],
    [
      "com.jexam.model",
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
