package chess;

import java.util.Objects;

public class ChessMove {
    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    public ChessPosition getStartPosition() {

        return startPosition;
    }

    public ChessPosition getEndPosition() {

        return endPosition;
    }

    public ChessPiece.PieceType getPromotionPiece() {

        return promotionPiece;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessMove)) return false;
        ChessMove move = (ChessMove) o;
        return Objects.equals(startPosition, move.startPosition) &&
               Objects.equals(endPosition, move.endPosition) &&
               promotionPiece == move.promotionPiece;
    }

    @Override
    public int hashCode() {

        return Objects.hash(startPosition, endPosition, promotionPiece);
    }

    @Override
    public String toString() {
        return "Move from " + startPosition + " to " + endPosition +
               (promotionPiece != null ? ", promote to " + promotionPiece : "");
    }
}
