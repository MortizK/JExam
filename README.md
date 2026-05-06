# JExam - Exam Management System

[![Java Version](https://img.shields.io/badge/Java-17-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive.html)
[![Maven Build](https://img.shields.io/badge/Maven-3.6+-green)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**JExam** ist eine professionelle JavaFX-Desktop-Anwendung zur Verwaltung von XML-basierten Fragenbanken und zur automatisierten Generierung von Klausur-PDFs. Das Projekt verbindet moderne Java-Technologien mit praktischer Anwendbarkeit in Bildungseinrichtungen.

---

## 📋 Inhaltsverzeichnis

- [Überblick & Features](#-überblick--features)
- [Zielgruppen](#-zielgruppen)
- [Voraussetzungen](#-voraussetzungen)
- [Installation & Setup](#-installation--setup)
  - [Für Nutzer](#für-nutzer)
  - [Für Entwickler](#für-entwickler)
  - [Für Dozenten](#für-dozenten)
- [Benutzerhandbuch](#-benutzerhandbuch)
- [Entwicklerhandbuch](#-entwicklerhandbuch)
- [Projektstruktur](#-projektstruktur)
- [Architektur & Design](#-architektur--design)
- [Testing & Quality](#-testing--quality)
- [Lizenz](#-lizenz)
- [Support & Kontakt](#-support--kontakt)

---

## 🎯 Überblick & Features

### Was ist JExam?

JExam ist ein Exam Management System, das Dozenten und Kursbetreuer dabei unterstützt, Klausuren effizient zu verwalten und zu generieren. Das System basiert auf einer zentralen XML-basierten Fragenbank, aus der Klausuren mit individuellen Fragen zusammengestellt und als PDF exportiert werden können.

### Hauptfunktionalitäten

- **Fragenbank-Verwaltung**: Zentrale Verwaltung von Prüfungsfragen in XML-Format
- **Klausur-Zusammenstellung**: Intuitive GUI zur Erstellung von Klausuren mit beliebigen Fragen
- **PDF-Generierung**: Automatische Erstellung von druckfertigen Klausur-PDFs
- **Varianten-Support**: Verschiedene Aufgabenvarianten pro Frage
- **Kapitel-Organisation**: Strukturierung von Fragen nach Themengebieten
- **Validierung**: Automatische Überprüfung auf Konsistenz und Vollständigkeit
- **Lokalisierung**: Multi-Language-Support (Deutsch, Englisch)
- **Undo/Redo**: Volle Undo/Redo-Funktionalität für Benutzerkomfort

---

## 👥 Zielgruppen

### 1. **Endnutzer / Dozenten & Kursbetreuer**
- Erstellen und verwalten Fragenbanken
- Generieren Klausuren für ihre Kurse
- Exportieren Klausuren als PDF zum Druck
- Benötigen keine Programmierkenntnisse

### 2. **Entwickler & Contributors**
- Erweitern die Funktionalität des Systems
- Beheben Bugs und verbessern Performance
- Integrieren neue Features
- Interessiert an modernem Java-Design und Best Practices

### 3. **Dozenten im Informatik-Unterricht**
- Verwenden JExam als Fallstudie für Softwareentwicklung
- Lehren agile Methoden und Clean Code
- Benutzten das Projekt als Referenzimplementierung
- Schätzen die dokumentierte Architektur und Use-Cases

---

## 📦 Voraussetzungen

### Systemanforderungen

| Komponente | Anforderung |
|---|---|
| **Betriebssystem** | Windows, macOS, Linux (mit grafischer Umgebung) |
| **RAM** | Mindestens 512 MB (1 GB empfohlen) |
| **Festplatte** | Mind. 200 MB freier Speicherplatz |
| **Display** | Mindestens 1024x768 Auflösung |

### Software-Anforderungen

**Für alle Nutzer:**
- Java Runtime Environment (JRE) 17 oder höher

**Für Entwickler zusätzlich:**
- Java Development Kit (JDK) 17 oder höher
- Apache Maven 3.6 oder höher
- Git (optional, für Versionskontrolle)
- IDE empfohlen: IntelliJ IDEA, Eclipse oder VS Code

---

## 🚀 Installation & Setup

### Für Nutzer

#### Option 1: Vorkompiliertes JAR (einfachste Methode)

1. **Java installieren** (falls nicht vorhanden):
	- Laden Sie JRE 17+ herunter: https://www.oracle.com/java/technologies/downloads/
	- Installieren Sie JRE mit den Standard-Einstellungen

2. **JExam starten**:
	```bash
	java -jar jexam-0.1.0.jar
	```
	Alternativ: Doppelklick auf die JAR-Datei (falls konfiguriert)

#### Option 2: Aus den Quellen kompilieren

```bash
# Repository klonen
git clone <repository-url>
cd JExam

# Projekt kompilieren
mvn clean package

# Anwendung starten
java -jar target/jexam-0.1.0.jar
```

---

### Für Entwickler

#### Schritt 1: Voraussetzungen installieren

**Windows:**
```powershell
# JDK 17 installieren
# Von https://www.oracle.com/java/technologies/downloads/ oder
# Nutze Chocolatey (falls installiert):
choco install openjdk17 maven
```

**macOS:**
```bash
# Mit Homebrew
brew install openjdk@17 maven
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get update
sudo apt-get install openjdk-17-jdk maven
```

#### Schritt 2: Repository klonen & Setup

```bash
# Repository klonen
git clone <repository-url>
cd JExam

# Abhängigkeiten herunterladen und Projekt kompilieren
mvn clean install
```

#### Schritt 3: IDE konfigurieren

**IntelliJ IDEA:**
1. `File → Open` → Ordner `JExam` auswählen
2. Maven wird automatisch erkannt
3. `File → Project Structure → SDK` → JDK 17 auswählen
4. Projekt Reload durchführen: `View → Tool Windows → Maven`

**Eclipse:**
1. `File → Import → Existing Maven Projects`
2. Ordner `JExam` auswählen
3. Finish klicken
4. `Right-click → Properties → Java Build Path → Libraries` → JDK 17 setzen

**VS Code:**
1. Extensions installieren: "Extension Pack for Java" von Microsoft
2. Ordner `JExam` öffnen
3. Maven-Abhängigkeiten werden automatisch gelöst

#### Schritt 4: Projekt bauen & testen

```bash
# Clean Build
mvn clean package

# Nur Tests ausführen
mvn test

# Klausur-Reports nach Maven Build
mvn test surefire-report:report
# Reports unter: target/site/surefire-report.html

# Code Coverage erzeugen
mvn jacoco:report
# Coverage Report: target/site/jacoco/index.html

# Qualitätsanalyse (Code Style, PMD)
mvn checkstyle:check pmd:check

# Kompletter Build mit allen Analysen
mvn clean package jacoco:report
```

#### Schritt 5: Anwendung entwickeln

```bash
# Im Debug-Modus ausführen (in IDE oder Terminal)
# In IDE: Debug-Button oder Tastenkombination (usualy F5)

# oder im Terminal (IDE muss nicht laufen):
mvn exec:java -Dexec.mainClass="com.jexam.app.JExamApplication"
```

---

## 📖 Benutzerhandbuch

### Anwendung starten

Nach dem Starten sehen Sie die Hauptoberfläche mit folgenden Bereichen:

```
┌────────────────────────────────────────┐
│  JExam - Exam Management System        │
├────────┬───────────────────────────────┤
│ Menü   │                               │
├────────┼───────────────────────────────┤
│        │   Hauptbereich der Anwendung  │
│ Baum   │                               │
│ View   │   (abhängig vom aktiven Tab)  │
│        │                               │
└────────┴───────────────────────────────┘
```

### Grundlegende Workflows

#### 1. Fragenbank erstellen oder öffnen

```
Menü → File → New/Open
 ↓
Fragenbank wird geladen (XML-Format)
 ↓
Struktur wird im Baum angezeigt
```

**XML-Format Beispiel:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<exam>
  <chapter name="Kapitel 1: Grundlagen">
	 <task id="task-001" name="Aufgabe 1">
		<variant id="v1">
		  <question>Beispielfrage...</question>
		</variant>
	 </task>
  </chapter>
</exam>
```

#### 2. Klausur zusammenstellen

```
Menü → Create New Exam
 ↓
Fragen per Drag-and-Drop oder Auswahl hinzufügen
 ↓
Reihenfolge anpassen
 ↓
Vorschau anschauen
```

#### 3. Klausur als PDF exportieren

```
Menü → Export → As PDF
 ↓
PDF-Einstellungen wählen (Deckblatt, Lösungen, etc.)
 ↓
Zielordner auswählen
 ↓
Speichern
```

### Tastenkombinationen

| Aktion | Tastenkombination |
|--------|-------------------|
| Rückgängig | `Ctrl+Z` |
| Wiederherstellen | `Ctrl+Y` |
| Speichern | `Ctrl+S` |
| Öffnen | `Ctrl+O` |
| Neu | `Ctrl+N` |
| Exportieren | `Ctrl+E` |
| Beenden | `Ctrl+Q` |

### Häufige Probleme

**Problem: Anwendung startet nicht**
- Lösung: Überprüfen Sie, ob Java 17+ installiert ist: `java -version`
- Falls nicht: JDK von https://www.oracle.com/java/ installieren

**Problem: PDF wird nicht korrekt generiert**
- Lösung: Validieren Sie die Fragenbank (Menü → Validate)
- Überprüfen Sie, ob alle erforderlichen Felder gefüllt sind

**Problem: Dateien können nicht geöffnet werden**
- Lösung: Überprüfen Sie, ob die XML-Datei valide ist
- Nutzen Sie einen XML-Validator online oder lokal

---

## 👨‍💻 Entwicklerhandbuch

### Projektstruktur

```
JExam/
├── src/
│   ├── main/java/com/jexam/
│   │   ├── app/                    # UI-Schicht (12+ Klassen)
│   │   │   ├── JExamApplication.java
│   │   │   ├── ui/
│   │   │   │   ├── screens/        # Verschiedene Screens/Views
│   │   │   │   └── components/     # Wiederverwendbare UI-Komponenten
│   │   │   └── services/           # UI-Services (ExamApplicationService)
│   │   │
│   │   ├── model/                  # Geschäftslogik & Datenmodell
│   │   │   ├── Exam.java           # Haupt-Entity
│   │   │   ├── Chapter.java
│   │   │   ├── Task.java
│   │   │   ├── Variant.java
│   │   │   └── enums/
│   │   │
│   │   ├── validation/             # Validierungslogik
│   │   │   ├── ExamValidator.java
│   │   │   └── ValidationResult.java
│   │   │
│   │   ├── io/                     # Ein- und Ausgabe
│   │   │   ├── ExamXmlLoader.java
│   │   │   ├── ExamXmlWriter.java
│   │   │   └── ExamPersistenceService.java
│   │   │
│   │   └── generation/             # PDF-Generierung
│   │       ├── PdfBoxGenerationService.java
│   │       └── PdfExportConfig.java
│   │
│   ├── main/resources/
│   │   ├── styles/                 # JavaFX CSS
│   │   ├── i18n/                   # Lokalisierungsdateien
│   │   └── config/                 # Standard-Konfiguration
│   │
│   └── test/java/                  # Unit-Tests (23 Klassen)
│       ├── com/jexam/app/          # UI-Tests
│       ├── com/jexam/io/           # IO-Tests
│       ├── com/jexam/model/        # Model-Tests
│       ├── com/jexam/generation/   # PDF-Generation Tests
│       ├── com/jexam/validation/   # Validation Tests
│       └── com/jexam/workflow/     # Integrations-Tests
│
├── doc/                            # Dokumentation
│   ├── 01_Analysefragen.pdf        # Anforderungsanalyse
│   ├── 02_Spezifikation.pdf        # Funktionale Spezifikation
│   ├── 03_Entwurf.pdf              # Architektur & Design
│   ├── Refactoring_Report.pdf      # Code-Refactoring Bericht
│   └── Tickets.pdf                 # Issue-Tracking
│
├── pom.xml                         # Maven-Konfiguration
├── README.md                       # Dieses Dokument
└── LICENSE                         # Apache 2.0 Lizenz
```

### Schichten-Architektur

```
┌─────────────────────────────────┐
│   Presentation Layer            │
│   (JavaFX GUI - app/ui/)        │
├─────────────────────────────────┤
│   Application Layer             │
│   (Services - app/services/)    │
├─────────────────────────────────┤
│   Business Logic Layer          │
│   (Model, Validation)           │
├─────────────────────────────────┤
│   Persistence Layer             │
│   (XML IO, PDF Generation)      │
└─────────────────────────────────┘
```

### Wichtige Klassen & ihre Rollen

| Klasse | Package | Verantwortung |
|--------|---------|---------------|
| `JExamApplication` | app | Entry-Point, JavaFX Bootstrap |
| `ExamApplicationService` | app.services | Business-Logik & Koordination |
| `Exam` | model | Haupt-Datenmodell |
| `ExamValidator` | validation | Konsistenz-Validierung |
| `ExamXmlLoader/Writer` | io | XML Serialisierung |
| `PdfBoxGenerationService` | generation | PDF-Erzeugung |

### Coding Standards

#### Java Version & Style
- **Java Version:** 17 (mit modernen Features wie Records, sealed classes)
- **Code Style:** Sun-Codestyle über Checkstyle (`mvn checkstyle:check`)
- **Naming:** PascalCase für Klassen, camelCase für Variablen
- **Javadoc:** Alle public Klassen und Methoden müssen dokumentiert sein

```java
/**
 * Kurzbeschreibung.
 *
 * @param param1 Beschreibung
 * @return Rückgabewert
 * @throws ExceptionType wenn ... auftritt
 */
public void myMethod(String param1) throws ExceptionType { }
```

#### Architektur-Regeln
- **No Cross-Schicht-Dependencies:** UI darf nicht direkt auf DAO/Persistence zugreifen
- **Dependency Injection:** Nutze Constructor-Injection statt Static Dependencies
- **Model Immutability:** Models (Exam, Chapter) sollten wenn möglich immutable sein
- **Error Handling:** ValidationResult statt Exceptions für Business-Fehler

### Test-Struktur

Alle Tests sind JUnit 5 basiert:

```bash
# Alle Tests ausführen
mvn test

# Spezifische Test-Klasse
mvn test -Dtest=ExamValidatorTest

# Mit Code-Coverage
mvn test jacoco:report
```

Test-Categories:
- **Unit-Tests:** Isolierte Test einzelner Klassen
- **Integration-Tests:** Multi-Klassen-Workflows
- **UI-Tests:** JavaFX-Komponenten-Tests

### Development-Workflow

1. **Feature-Branch erstellen:**
	```bash
	git checkout -b feature/my-feature
	```

2. **Lokal entwickeln & testen:**
	```bash
	mvn clean test              # Unit-Tests
	mvn integration-test        # Integrations-Tests
	mvn checkstyle:check        # Code-Style
	```

3. **Code-Coverage prüfen:**
	```bash
	mvn jacoco:report
	# Öffnen: target/site/jacoco/index.html
	```

4. **Commits & Push:**
	```bash
	git add .
	git commit -m "feat: descriptive message"
	git push origin feature/my-feature
	```

5. **Pull Request erstellen & Code Review durchführen**

---

## 🏗️ Architektur & Design

### Design Patterns

| Pattern | Verwendung | Beispiel |
|---------|-----------|----------|
| MVC | Separation UI/Logic | ExamApplicationService + UI |
| Service Locator | Dependency Management | ExamApplicationService |
| Factory | Object-Erstellung | ExamFactory |
| Strategy | Alternative Algorithmen | PdfExportStrategy |
| Observer | Change Notification | JavaFX Bindings |

### Datenfluss

```
User Input (UI)
	 ↓
[ExamApplicationService]
	 ↓
[Validierung]
	 ↓
[Model ändern]
	 ↓
[Persistierung (XML/PDF)]
	 ↓
Dateisystem
```

### Exception Handling

Business-Fehler werden durch `ValidationResult` modelliert, nicht durch Exceptions:

```java
ValidationResult result = ExamValidator.validate(exam);
if (!result.isValid()) {
	 result.getErrors().forEach(System.out::println);
}
```

---

## 🧪 Testing & Quality

### Test Coverage

Zielwerte für Code-Coverage:
- **Insgesamt:** > 75%
- **Core Logic (validation, io):** > 85%
- **UI:** > 50% (UI-Tests sind schwer zu automatisieren)

Current Coverage: Siehe `target/site/jacoco/index.html` nach `mvn jacoco:report`

### Testausführung

```bash
# Alle Tests
mvn test

# Spezifische Test-Klasse
mvn test -Dtest=ExamValidatorTest

# Mit Standard-Output
mvn test -X

# Schnelle Tests nur (Skipping langsame Tests)
mvn test -DskipSlowTests
```

### Kontinuierliche Integration

Das Projekt ist vorbereitet für CI/CD:

```yaml
# Build-Pipeline (z.B. GitHub Actions)
mvn clean verify
mvn jacoco:report
mvn site
```

---

## 📄 Lizenz

JExam ist unter der **Apache License 2.0** lizenziert. Siehe [LICENSE](LICENSE) für Details.

Kurzzusammenfassung (nicht haftend für die [LICENSE](LICENSE)):
- ✅ Kommerziell nutzbar
- ✅ Modifizierbar
- ✅ Verteilbar
- ❌ Keine Haftung
- ⚠️ Muss LICENSE & NOTICE beifügt werden

---

## 📞 Support & Kontakt

### Dokumentation

Detaillierte Dokumentation finden Sie im `doc/` Ordner:
- **01_Analysefragen.pdf** - Anforderungen & Use Cases
- **02_Spezifikation.pdf** - Funktionale Spezifikation
- **03_Entwurf.pdf** - Technische Architektur
- **Refactoring_Report.pdf** - Überblick Code-Änderungen
- **Tickets.pdf** - Issue-Tracking & Prioritäten

### Für Entwickler & Dozenten

**GitHub Issues:** Fehler berichten & Features vorschlagen
```
GitHub → Issues → New Issue
Template ausfüllen und absenden
```

**Pull Requests:** Beiträge einreichen
```
Git Workflow wie oben beschrieben
```

### Häufig gestellte Fragen

**F: Kann ich JExam für ein kommerzielles Projekt nutzen?**
A: Ja, unter Apache 2.0 Lizenz ist das gestattet. Sie müssen nur LICENSE & NOTICE beifügen.

**F: Wie kann ich beitragen?**
A: Forken Sie das Repository, erstellen Sie einen Feature-Branch, und reichen Sie einen Pull Request ein.

**F: Welche Anforderungen gibt es an XML-Dateien?**
A: Siehe `doc/02_Spezifikation.pdf` für XML-Schema und Anforderungen.

**F: Wo finde ich Beispiele für Fragenbanken?**
A: Im `src/test/resources` Verzeichnis gibt es vordefinierte Beispiele.

---

## 📊 Projektmetriken

| Metrik | Wert |
|--------|------|
| Java-Dateien gesamt | 78 |
| Java-Dateien in `src/main/java` | 55 |
| Java-Dateien in `src/test/java` | 23 |
| Pakete gesamt | 14 |
| Klassen/Interfaces/Enums/Records | 24 |
| Methoden/Konstruktoren | 633 |
| Code-Zeilen | 7.867 |
| Kommentarzeilen | 2.101 |
| Leerzeilen | 1.421 |
| Kommentaranteil | 18,45 % |
| Durchschnittliche Methodengröße | 12,43 Code-Zeilen |
| Geschätzte zyklomatische Komplexität je Methode | 2,08 |
| Interne Paketabhängigkeiten | 34 |
| Java Version | 17 |
| Abhängigkeiten | 3 (JavaFX, PDFBox, JUnit) |

Stand: 06.05.2026 13:10

---

## 🗺️ Roadmap

### Version 0.1.0 (Current)
- ✅ Basis CRUD für Fragenbanken
- ✅ XML Import/Export
- ✅ PDF-Generierung
- ✅ Validierung

### Geplante Features
- 🔄 Multi-Language Support erweitern
- 🔄 Template-System für PDF-Layouts

---

**Zuletzt aktualisiert:** 06.05.2026
