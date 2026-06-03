package com.example.botservice.bot;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The bot's brain. Given the board, pick a column for the bot (player 2).
 * Priority (a simple but convincing heuristic for the demo):
 *   1. If the bot can win this move, take it.
 *   2. If the opponent could win next move, block that column.
 *   3. Otherwise prefer the centre (strongest in Connect Four), with a little
 *      randomness so games vary.
 *
 * Board layout matches Game Service: 7 cols x 6 rows, row 0 is the bottom,
 * 0 = empty, 1 = human, 2 = bot.
 */
@Component
public class BotStrategy {

    public static final int COLS = 7;
    public static final int ROWS = 6;
    private static final int BOT = 2;
    private static final int HUMAN = 1;

    private final Random random = new Random();

    public int chooseColumn(int[][] board) {
        // 1. Win if we can.
        for (int c = 0; c < COLS; c++) {
            if (canDrop(board, c) && wouldWin(board, c, BOT)) {
                return c;
            }
        }
        // 2. Block the human's winning move.
        for (int c = 0; c < COLS; c++) {
            if (canDrop(board, c) && wouldWin(board, c, HUMAN)) {
                return c;
            }
        }
        // 3. Prefer centre, then nearest-to-centre columns.
        int[] preference = {3, 2, 4, 1, 5, 0, 6};
        List<Integer> playable = new ArrayList<>();
        for (int c : preference) {
            if (canDrop(board, c)) {
                playable.add(c);
            }
        }
        if (playable.isEmpty()) {
            return -1; // board full; should not happen if a move is expected
        }
        // Mostly pick the best available, occasionally a random playable column.
        if (random.nextInt(100) < 80) {
            return playable.get(0);
        }
        return playable.get(random.nextInt(playable.size()));
    }

    private boolean canDrop(int[][] board, int col) {
        return col >= 0 && col < COLS && board[ROWS - 1][col] == 0;
    }

    /** Simulate dropping for {@code player} in {@code col} and test for a win. */
    private boolean wouldWin(int[][] board, int col, int player) {
        int[][] copy = copy(board);
        int row = drop(copy, col, player);
        if (row < 0) return false;
        return hasWon(copy, player);
    }

    private int[][] copy(int[][] board) {
        int[][] c = new int[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            System.arraycopy(board[r], 0, c[r], 0, COLS);
        }
        return c;
    }

    private int drop(int[][] board, int col, int player) {
        for (int r = 0; r < ROWS; r++) {
            if (board[r][col] == 0) {
                board[r][col] = player;
                return r;
            }
        }
        return -1;
    }

    private boolean hasWon(int[][] board, int player) {
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] != player) continue;
                for (int[] d : dirs) {
                    int count = 0, rr = r, cc = c;
                    while (rr >= 0 && rr < ROWS && cc >= 0 && cc < COLS && board[rr][cc] == player) {
                        count++; rr += d[0]; cc += d[1];
                    }
                    if (count >= 4) return true;
                }
            }
        }
        return false;
    }

    public static int[][] parse(String boardCsv) {
        int[][] board = new int[ROWS][COLS];
        if (boardCsv == null || boardCsv.isBlank()) return board;
        String[] cells = boardCsv.split(",");
        for (int i = 0; i < ROWS * COLS && i < cells.length; i++) {
            board[i / COLS][i % COLS] = Integer.parseInt(cells[i].trim());
        }
        return board;
    }
}
