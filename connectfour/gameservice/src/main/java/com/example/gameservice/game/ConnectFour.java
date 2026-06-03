package com.example.gameservice.game;

/**
 * Pure Connect Four rules engine. No framework dependencies, so it is trivial
 * to unit test and easy to explain. The board is 7 columns x 6 rows.
 *
 * Cell values: 0 = empty, 1 = player one (red), 2 = player two / bot (yellow).
 * Row 0 is the BOTTOM row, so a dropped disc falls to the lowest empty row.
 */
public final class ConnectFour {

    public static final int COLS = 7;
    public static final int ROWS = 6;

    private ConnectFour() {
    }

    /** Parse the stored board string ("0,0,1,...") into a 2D array [row][col]. */
    public static int[][] parse(String boardCsv) {
        int[][] board = new int[ROWS][COLS];
        if (boardCsv == null || boardCsv.isBlank()) {
            return board;
        }
        String[] cells = boardCsv.split(",");
        for (int i = 0; i < ROWS * COLS && i < cells.length; i++) {
            board[i / COLS][i % COLS] = Integer.parseInt(cells[i].trim());
        }
        return board;
    }

    /** Serialise a board back to CSV for storage. */
    public static String serialize(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (sb.length() > 0) sb.append(',');
                sb.append(board[r][c]);
            }
        }
        return sb.toString();
    }

    public static boolean columnInRange(int col) {
        return col >= 0 && col < COLS;
    }

    /** True if the column has at least one empty slot. */
    public static boolean canDrop(int[][] board, int col) {
        return columnInRange(col) && board[ROWS - 1][col] == 0;
    }

    /**
     * Drop a disc for {@code player} into {@code col}.
     * @return the row it landed in, or -1 if the column is full / invalid.
     */
    public static int drop(int[][] board, int col, int player) {
        if (!columnInRange(col)) return -1;
        for (int r = 0; r < ROWS; r++) {
            if (board[r][col] == 0) {
                board[r][col] = player;
                return r;
            }
        }
        return -1;
    }

    /** True if every top cell is filled. */
    public static boolean isFull(int[][] board) {
        for (int c = 0; c < COLS; c++) {
            if (board[ROWS - 1][c] == 0) return false;
        }
        return true;
    }

    /**
     * Did {@code player} just make four in a row anywhere? Checks the four
     * directions: horizontal, vertical, and both diagonals.
     */
    public static boolean hasWon(int[][] board, int player) {
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] != player) continue;
                for (int[] d : directions) {
                    if (countLine(board, r, c, d[0], d[1], player) >= 4) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int countLine(int[][] board, int r, int c, int dr, int dc, int player) {
        int count = 0;
        int rr = r, cc = c;
        while (rr >= 0 && rr < ROWS && cc >= 0 && cc < COLS && board[rr][cc] == player) {
            count++;
            rr += dr;
            cc += dc;
        }
        return count;
    }
}
