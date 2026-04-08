# GitHub Wiki Export

This repository keeps wiki source files in `wiki/`.

For publishing to GitHub Wiki, generate an export folder that contains only wiki pages and GitHub special files.

## Export Command

From repository root:

    powershell -ExecutionPolicy Bypass -File .\scripts\export-wiki.ps1

## Output

Export folder:

- `wiki/github-wiki-export/`

Included files:

- `Home.md`
- `_Sidebar.md`
- `_Footer.md`
- All content pages from `wiki/` except index and export helper pages

## Why This Exists

- Keeps authoring pages in-repo.
- Produces a clean folder that can be copied into the `<repo>.wiki.git` repository.
- Avoids accidental publishing of internal index files.
