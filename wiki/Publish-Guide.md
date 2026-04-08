# Publish Guide

This page describes publishing the generated export to GitHub Wiki.

## 1. Generate Export

    powershell -ExecutionPolicy Bypass -File .\scripts\export-wiki.ps1

## 2. Clone Wiki Repository

Replace `OWNER` and `REPO`:

    git clone https://github.com/OWNER/REPO.wiki.git

## 3. Copy Exported Files

Copy everything from `wiki/github-wiki-export/` into the cloned wiki repository root.

## 4. Commit And Push

Inside the cloned wiki repository:

    git add .
    git commit -m "Update developer wiki"
    git push

## Notes

- GitHub Wiki uses `Home.md` as the landing page.
- `_Sidebar.md` controls the left sidebar navigation.
- `_Footer.md` controls footer content on pages.
