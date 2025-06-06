# CONTEXT.md

## 📘 Project Name
SeedV4 – Java UCI Chess Engine

## 🎯 Purpose
Build a clean, scalable, and efficient Java chess engine using bitboards, flattened negamax search, and minimal class overhead. The engine will be tested via UCI protocol (Arena/CuteChess) and eventually support advanced features like transposition tables and NNUE.

## 🧱 Tech Stack
- Java 21
- Gradle (Groovy DSL)
- Git + GitHub
- Codex for implementation
- No Java concurrency libraries
- No GUI (UCI only)

## 🧩 Core Modules
- `UCIEngine`: UCI protocol handler
- `Board`: Bitboard state + Zobrist hashing
- `Gen`: Bitboard-based move generator (including magic bitboards)
- `Eval`: Evaluation with material and PST
- `Search`: Flattened negamax alpha-beta, with quiescence
- `Util`: Helper constants and bitboard logic
- `Book`: Stubbed opening book interface
- `Perft`: Perft validation for move generation

## 💡 Design Priorities
- Flat, minimal architecture
- No runtime dependencies outside JDK
- Custom concurrency and stack handling if required
- Codex-led coding; user-guided architecture and review
