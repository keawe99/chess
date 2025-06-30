package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    private void addSlidingMoves(ChessBoard board, ChessPosition from, List<ChessMove> moves, int[][] directions) {
    for (int[] dir : directions) {
        int r = from.getRow();
        int c = from.getColumn();
        while (true) {
            r += dir[0];
            c += dir[1];
            if (!isInBounds(r, c)) break;
            ChessPosition to = new ChessPosition(r, c);
            ChessPiece target = board.getPiece(to);
            if (target == null) {
                moves.add(new ChessMove(from, to, null));
            } else {
                if (target.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(from, to, null));
                }
                break;
            }
        }
    }
}

private boolean isInBounds(int row, int col) {
    return row >= 1 && row <= 8 && col >= 1 && col <= 8;
}

private void addIfValidMove(ChessBoard board, ChessPosition from, ChessPosition to, List<ChessMove> moves) {
    ChessPiece target = board.getPiece(to);
    if (target == null || target.getTeamColor() != this.pieceColor) {
        moves.add(new ChessMove(from, to, null));
    }
}

private void addPawnMove(ChessPosition from, int row, int col, int endRow, List<ChessMove> moves) {
    ChessPosition to = new ChessPosition(row, col);
    if (row == endRow) {
        for (ChessPiece.PieceType promo : List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
            moves.add(new ChessMove(from, to, promo));
        }
    } else {
        moves.add(new ChessMove(from, to, null));
    }
}


    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    public PieceType getPieceType() {
        return type;
    }

    // Implement equals and hashCode — required for tests to work correctly

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessPiece)) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return pieceColor + " " + type;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
    int row = myPosition.getRow();
    int col = myPosition.getColumn();

    switch (this.type) {
        case BISHOP -> addSlidingMoves(board, myPosition, moves, new int[][]{
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        });

        case ROOK -> addSlidingMoves(board, myPosition, moves, new int[][]{
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        });

        case QUEEN -> addSlidingMoves(board, myPosition, moves, new int[][]{
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        });

        case KING -> {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int r = row + dr;
                    int c = col + dc;
                    if (isInBounds(r, c)) {
                        ChessPosition newPos = new ChessPosition(r, c);
                        addIfValidMove(board, myPosition, newPos, moves);
                    }
                }
            }
        }

        case KNIGHT -> {
            int[][] jumps = {
                    {2, 1}, {1, 2}, {-1, 2}, {-2, 1},
                    {-2, -1}, {-1, -2}, {1, -2}, {2, -1}
            };
            for (int[] jump : jumps) {
                int r = row + jump[0];
                int c = col + jump[1];
                if (isInBounds(r, c)) {
                    ChessPosition newPos = new ChessPosition(r, c);
                    addIfValidMove(board, myPosition, newPos, moves);
                }
            }
        }

        case PAWN -> {
            int direction = this.pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1;
            int startRow = this.pieceColor == ChessGame.TeamColor.WHITE ? 2 : 7;
            int endRow = this.pieceColor == ChessGame.TeamColor.WHITE ? 8 : 1;

            // Forward move
            int oneAhead = row + direction;
            if (isInBounds(oneAhead, col) && board.getPiece(new ChessPosition(oneAhead, col)) == null) {
                addPawnMove(myPosition, oneAhead, col, endRow, moves);

                // Double move from starting position
                int twoAhead = row + 2 * direction;
                if (row == startRow && board.getPiece(new ChessPosition(twoAhead, col)) == null) {
                    moves.add(new ChessMove(myPosition, new ChessPosition(twoAhead, col), null));
                }
            }

            // Diagonal captures
            for (int dc = -1; dc <= 1; dc += 2) {
                int newCol = col + dc;
                if (isInBounds(oneAhead, newCol)) {
                    ChessPosition diag = new ChessPosition(oneAhead, newCol);
                    ChessPiece target = board.getPiece(diag);
                    if (target != null && target.pieceColor != this.pieceColor) {
                        addPawnMove(myPosition, oneAhead, newCol, endRow, moves);
                    }
                }
            }
        }

        default -> throw new UnsupportedOperationException("Unhandled piece type: " + this.type);
    }

    return moves;
}
    public enum PieceType {
        BISHOP, ROOK, KNIGHT, QUEEN, KING, PAWN
    }
}
