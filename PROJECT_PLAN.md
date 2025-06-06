# PROJECT_PLAN.md

## 🎯 Project Goal

Develop a **UCI-compliant Java chess engine** using bitboard representation, flattened negamax search, and scalable architecture suitable for future enhancements such as parallel search, transposition tables, opening books, and endgame tablebases. All code will be written by Codex under user guidance.

---

## 🔧 Core Design Principles

- No GUI: Engine will be tested via **UCI protocol** in tools like **Arena** or **CuteChess**.
- No over-engineering: Minimal classes, static utility style preferred.
- All code will avoid use of Java concurrency libraries unless absolutely required.
- Scalable: Designed for easy upgrade to parallelism, TT, books, etc.

---

## 📦 Project Structure

```
engine/
├── UCIEngine.java    # Entry point, stdin-based UCI protocol handler
├── Board.java        # Static utility class: board representation and Zobrist handling
├── Gen.java          # Bitboard-based move generation, including magic bitboards
├── Eval.java         # Basic material + PST evaluation
├── Search.java       # Flattened negamax alpha-beta, with custom stack
├── Util.java         # Bitboard constants and helper methods
├── Book.java         # (stub) Interface to opening book
└── Perft.java        # Move generation tester
```

---

## ♟ Board Representation

- `long[] board = new long[4]`: holds bitboards for pieces.
  - Index 0: white pawns
  - Index 1: white other
  - Index 2: black pawns
  - Index 3: black other
- `long state`: packed:
  - Bits:
    - [0]: player to move
    - [1–4]: castling rights
    - [5–10]: en passant square
    - [11–17]: half-move clock
    - [18–27]: full-move number
- `long zobristKey`: position hash

---

## 🔍 Search

### Algorithm
- **Negamax** with alpha-beta pruning
- **Flattened**, non-recursive
- Custom stack using `SearchFrame[]` object
- **Quiescence search** at horizon
- Stubs for:
  - Transposition table
  - Move ordering (killer/history)

### Parallelism
- Custom thread management (no `java.util.concurrent`)
- Each thread uses its own stack and config

---

## 🧠 Evaluation

- Simple **material score**
- Basic **piece-square tables** (PSTs)

---

## 🔌 UCI Protocol

Supported commands:
- `uci`
- `isready`
- `ucinewgame`
- `position fen ...`
- `go` and `stop`
- `quit`

---

## 🧪 Testing

### Perft Support
- Implement `Perft.perft(position, depth)` to:
  - Recursively count nodes from position
  - Validate move generation
  - Run with known test positions

---

## ⏳ Planned Features

- Transposition table
- Null move pruning
- Killer and history heuristics
- Opening book (via PGN/EPD)
- Endgame tablebases
- GUI wrapper (optional)
- MCTS/NNUE integration (long-term)

---

## ✅ Development Flow

- Tasks defined via GitHub issues or markdown TODOs
- Codex handles all coding
- User reviews, clarifies, or edits as needed
- Each module verified via Perft or unit testing where applicable

---

*Generated on 2025-06-06 by ChatGPT based on user design.*
