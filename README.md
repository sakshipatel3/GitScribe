<p align="center">
<img src="https://capsule-render.vercel.app/api?type=waving&color=0:6a11cb,100:2575fc&height=200&section=header&text=GitScribe&fontSize=45&fontColor=ffffff"/>
</p>

# GitScribe 🚀🔍

**GitScribe** is a developer tool that mines Git history to track how Java methods evolve across commits.

Instead of just looking at file-level changes, GitScribe focuses on **method-level evolution** — helping developers understand *how code actually changes over time.*

---

## 🛠 Tech Stack

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Git](https://img.shields.io/badge/Git-181717?style=for-the-badge&logo=git&logoColor=white)
![AST](https://img.shields.io/badge/AST%20Parsing-blue?style=for-the-badge)

---

## ✨ Key Features

### 📜 Method History Extraction
Track a method across commits, even through **refactorings and file renames**.

### ⚙️ Change Detection Engine
Automatically detects multiple types of method edits:

- 🔧 Body Changes  
- 🧾 Parameter Changes  
- 🔁 Return-Type Changes  
- 🛡 Modifier Changes  
- ⚠️ Exception Changes  
- 📂 File Renames  
- ➕ Introduced / ➖ Deleted Methods  
- 🔀 MultiChange Events  

### 🔄 Cross-Repository Analysis
Analyze and compare **method evolution patterns across multiple projects**.

---
## 🏗 Architecture

GitScribe follows a modular pipeline architecture that processes repositories,
tracks method evolution, detects change types, and exports structured results.

<p align="center">
<img src="images/architecture.png" width="750"/>
</p>

The system consists of five main layers:

• **Orchestration Layer** – coordinates repository processing  
• **VCS Adapter** – clones repositories using JGit  
• **History Traversal Engine** – walks commit history and tracks file changes  
• **Structural Analysis** – parses Java methods using Eclipse JDT AST  
• **Change Detection Pipeline** – identifies method-level changes  
• **Reporting Layer** – exports results as structured CSV files

## 💡 Why Use GitScribe?

### 🔍 Understand Real Code Evolution
Discover where developers actually spend their time during development.

Example insights:
- Body edits dominate most commits
- Certain change types frequently appear together

### 🛠 Improve Development Tools
Use real change patterns to design **smarter refactoring tools and developer workflows.**

### 📊 Benchmark Research
Evaluate new **change-detection algorithms** on real commit history data.

### 📚 Teaching Software Evolution
Perfect for demonstrating **how software evolves in real-world projects.**

---

## 🚀 Example Use Cases

- Analyzing refactoring patterns in large repositories
- Mining commit histories for research
- Understanding method-level software evolution
- Generating datasets for change detection research

---

⭐ If you found this project interesting, consider giving it a star!
