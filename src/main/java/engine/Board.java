package engine;

import java.util.Arrays;
import java.util.Random;

/**
 * Static utility class for board representation and Zobrist hashing.
 */
public final class Board {
    private Board() {}

    // Piece constants (bits 0-2)
    public static final int KING = 1;
    public static final int QUEEN = 2;
    public static final int ROOK = 3;
    public static final int BISHOP = 4;
    public static final int KNIGHT = 5;
    public static final int PAWN = 6;

    // Bitboard indices
    private static final int BIT0 = 0;
    private static final int BIT1 = 1;
    private static final int BIT2 = 2;
    private static final int COLOR = 3;

    // State layout masks and shifts
    private static final long SIDE_MASK = 1L;
    private static final int SIDE_SHIFT = 0;

    private static final int CASTLE_SHIFT = 1;
    private static final long CASTLE_MASK = 0xFL << CASTLE_SHIFT;
    public static final int CASTLE_WK = 1 << 1;
    public static final int CASTLE_WQ = 1 << 2;
    public static final int CASTLE_BK = 1 << 3;
    public static final int CASTLE_BQ = 1 << 4;

    private static final int EP_SHIFT = 5;
    private static final long EP_MASK = 0x3FL << EP_SHIFT;
    public static final int EP_NONE = 0x3F;

    private static final int HALF_SHIFT = 11;
    private static final long HALF_MASK = 0x7FL << HALF_SHIFT;

    private static final int FULL_SHIFT = 18;
    private static final long FULL_MASK = 0x3FFL << FULL_SHIFT;

    // Board and state
    public static final long[] board = new long[4];
    public static long state;
    public static long zobristKey;

    // Zobrist tables
    private static final long[][][] ZOBRIST_PIECE = new long[64][7][2];
    private static final long[] ZOBRIST_CASTLE = new long[16];
    private static final long[] ZOBRIST_EP = new long[8];
    private static long ZOBRIST_SIDE;

    private static final String START_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    static {
        Random r = new Random(20240607L);
        for (int sq = 0; sq < 64; sq++) {
            for (int pt = 1; pt <= 6; pt++) {
                for (int c = 0; c < 2; c++) {
                    ZOBRIST_PIECE[sq][pt][c] = r.nextLong();
                }
            }
        }
        for (int i = 0; i < 16; i++) {
            ZOBRIST_CASTLE[i] = r.nextLong();
        }
        for (int i = 0; i < 8; i++) {
            ZOBRIST_EP[i] = r.nextLong();
        }
        ZOBRIST_SIDE = r.nextLong();

        setInitial();
    }

    // Piece helpers ---------------------------------------------------------
    public static int getPiece(int sq) {
        int p = 0;
        if (((board[BIT0] >>> sq) & 1L) != 0) p |= 1;
        if (((board[BIT1] >>> sq) & 1L) != 0) p |= 2;
        if (((board[BIT2] >>> sq) & 1L) != 0) p |= 4;
        if (((board[COLOR] >>> sq) & 1L) != 0) p |= 8;
        return p;
    }

    public static void clearSquare(int sq) {
        long mask = ~(1L << sq);
        for (int i = 0; i < 4; i++) {
            board[i] &= mask;
        }
    }

    public static void setPiece(int sq, int piece) {
        long mask = 1L << sq;
        for (int i = 0; i < 4; i++) {
            if (((piece >> i) & 1) != 0) {
                board[i] |= mask;
            } else {
                board[i] &= ~mask;
            }
        }
    }

    // ----------------------------------------------------------------------
    public static int getSideToMove() {
        return (int) ((state & SIDE_MASK) >>> SIDE_SHIFT);
    }

    public static void setSideToMove(int side) {
        state = (state & ~SIDE_MASK) | ((long) (side & 1) << SIDE_SHIFT);
    }

    public static int getCastlingRights() {
        return (int) ((state & CASTLE_MASK) >>> CASTLE_SHIFT);
    }

    public static void setCastlingRights(int rights) {
        state = (state & ~CASTLE_MASK) | ((long) (rights & 0xF) << CASTLE_SHIFT);
    }

    public static int getEnPassantSquare() {
        return (int) ((state & EP_MASK) >>> EP_SHIFT);
    }

    public static void setEnPassantSquare(int sq) {
        state = (state & ~EP_MASK) | ((long) (sq & 0x3F) << EP_SHIFT);
    }

    public static int getHalfMoveClock() {
        return (int) ((state & HALF_MASK) >>> HALF_SHIFT);
    }

    public static void setHalfMoveClock(int hm) {
        state = (state & ~HALF_MASK) | ((long) (hm & 0x7F) << HALF_SHIFT);
    }

    public static int getFullMoveNumber() {
        return (int) ((state & FULL_MASK) >>> FULL_SHIFT);
    }

    public static void setFullMoveNumber(int fm) {
        state = (state & ~FULL_MASK) | ((long) (fm & 0x3FF) << FULL_SHIFT);
    }

    // ----------------------------------------------------------------------
    public static void setInitial() {
        fromFEN(START_FEN);
    }

    private static char pieceToChar(int piece) {
        int type = piece & 7;
        int color = (piece >>> 3) & 1;
        char c;
        switch (type) {
            case KING -> c = 'k';
            case QUEEN -> c = 'q';
            case ROOK -> c = 'r';
            case BISHOP -> c = 'b';
            case KNIGHT -> c = 'n';
            case PAWN -> c = 'p';
            default -> { return ' '; }
        }
        if (color == 0) c = Character.toUpperCase(c);
        return c;
    }

    private static int charToPiece(char c) {
        int color = Character.isUpperCase(c) ? 0 : 8;
        switch (Character.toLowerCase(c)) {
            case 'k': return KING | color;
            case 'q': return QUEEN | color;
            case 'r': return ROOK | color;
            case 'b': return BISHOP | color;
            case 'n': return KNIGHT | color;
            case 'p': return PAWN | color;
            default: return 0;
        }
    }

    private static String squareToString(int sq) {
        char file = (char) ('a' + (sq & 7));
        char rank = (char) ('1' + (sq >>> 3));
        return "" + file + rank;
    }

    private static int stringToSquare(String s) {
        if (s.length() != 2) return EP_NONE;
        int file = s.charAt(0) - 'a';
        int rank = s.charAt(1) - '1';
        if (file < 0 || file > 7 || rank < 0 || rank > 7) return EP_NONE;
        return rank * 8 + file;
    }

    // FEN handling ----------------------------------------------------------
    public static String toFEN() {
        StringBuilder sb = new StringBuilder();
        for (int r = 7; r >= 0; r--) {
            int empty = 0;
            for (int f = 0; f < 8; f++) {
                int sq = r * 8 + f;
                int p = getPiece(sq);
                if (p == 0) {
                    empty++;
                } else {
                    if (empty > 0) {
                        sb.append(empty);
                        empty = 0;
                    }
                    sb.append(pieceToChar(p));
                }
            }
            if (empty > 0) sb.append(empty);
            if (r > 0) sb.append('/');
        }
        sb.append(' ');
        sb.append(getSideToMove() == 0 ? 'w' : 'b');
        sb.append(' ');
        int rights = getCastlingRights();
        if (rights == 0) {
            sb.append('-');
        } else {
            if ((rights & CASTLE_WK) != 0) sb.append('K');
            if ((rights & CASTLE_WQ) != 0) sb.append('Q');
            if ((rights & CASTLE_BK) != 0) sb.append('k');
            if ((rights & CASTLE_BQ) != 0) sb.append('q');
        }
        sb.append(' ');
        int ep = getEnPassantSquare();
        if (ep == EP_NONE) {
            sb.append('-');
        } else {
            sb.append(squareToString(ep));
        }
        sb.append(' ');
        sb.append(getHalfMoveClock());
        sb.append(' ');
        sb.append(getFullMoveNumber());
        return sb.toString();
    }

    public static void fromFEN(String fen) {
        Arrays.fill(board, 0L);
        state = 0L;
        String[] parts = fen.trim().split("\\s+");
        if (parts.length < 4) return;
        String[] ranks = parts[0].split("/");
        int sq = 56;
        for (String rank : ranks) {
            int file = 0;
            for (int i = 0; i < rank.length(); i++) {
                char c = rank.charAt(i);
                if (Character.isDigit(c)) {
                    file += c - '0';
                } else {
                    int p = charToPiece(c);
                    setPiece(sq + file, p);
                    file++;
                }
            }
            sq -= 8;
        }
        setSideToMove(parts[1].equals("w") ? 0 : 1);
        int rights = 0;
        if (!parts[2].equals("-")) {
            for (char c : parts[2].toCharArray()) {
                switch (c) {
                    case 'K' -> rights |= CASTLE_WK;
                    case 'Q' -> rights |= CASTLE_WQ;
                    case 'k' -> rights |= CASTLE_BK;
                    case 'q' -> rights |= CASTLE_BQ;
                }
            }
        }
        setCastlingRights(rights);
        if (!parts[3].equals("-")) {
            setEnPassantSquare(stringToSquare(parts[3]));
        } else {
            setEnPassantSquare(EP_NONE);
        }
        if (parts.length > 4) {
            setHalfMoveClock(Integer.parseInt(parts[4]));
        } else {
            setHalfMoveClock(0);
        }
        if (parts.length > 5) {
            setFullMoveNumber(Integer.parseInt(parts[5]));
        } else {
            setFullMoveNumber(1);
        }
        computeZobrist();
    }

    // ----------------------------------------------------------------------
    public static long computeZobrist() {
        long key = 0L;
        for (int sq = 0; sq < 64; sq++) {
            int p = getPiece(sq);
            if (p != 0) {
                int type = p & 7;
                int color = (p >>> 3) & 1;
                key ^= ZOBRIST_PIECE[sq][type][color];
            }
        }
        key ^= ZOBRIST_CASTLE[getCastlingRights()];
        int ep = getEnPassantSquare();
        if (ep != EP_NONE) {
            key ^= ZOBRIST_EP[ep & 7];
        }
        if (getSideToMove() == 1) {
            key ^= ZOBRIST_SIDE;
        }
        zobristKey = key;
        return key;
    }
}
