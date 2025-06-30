package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

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

    switch (this.type) {
        case BISHOP -> {
            int[][] directions = {
                    {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
            };

            for (int[] dir : directions) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();

                while (true) {
                    row += dir[0];
                    col += dir[1];

                    if (row < 1 || row > 8 || col < 1 || col > 8) break;

                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece targetPiece = board.getPiece(newPos);

                    if (targetPiece == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    } else {
                        if (targetPiece.getTeamColor() != this.pieceColor) {
                            moves.add(new ChessMove(myPosition, newPos, null));
                        }
                        break; // stop at first non-empty square
                    }
                }
            }
        }

        default -> throw new UnsupportedOperationException("Move generation not implemented for " + type);
    }

    return moves;
}

    public enum PieceType {
        BISHOP, ROOK, KNIGHT, QUEEN, KING, PAWN
    }
}
