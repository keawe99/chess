package chess;

import java.util.*;

/**
 * Manages the state and rules of a chess game.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    public ChessBoard getBoard() {
        return board;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) return null;

        Collection<ChessMove> pseudoMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : pseudoMoves) {
            ChessBoard simulated = cloneBoard(board);
            simulateMove(simulated, move);

            ChessPosition kingPos = findKingPosition(piece.getTeamColor(), simulated);
            if (!isPositionUnderAttack(kingPos, piece.getTeamColor(), simulated)) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece at start position");
        }

        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("It's not this piece's turn");
        }

        Collection<ChessMove> legalMoves = validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        // Apply move
        board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(),
                move.getPromotionPiece() != null ? move.getPromotionPiece() : piece.getPieceType()));
        board.addPiece(move.getStartPosition(), null);

        // Switch turns
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = findKingPosition(teamColor, board);
        return isPositionUnderAttack(kingPos, teamColor, board);
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) return false;

        return getAllLegalMoves(teamColor).isEmpty();
    }

    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) return false;

        return getAllLegalMoves(teamColor).isEmpty();
    }

    // Helper: Clone the board
    private ChessBoard cloneBoard(ChessBoard original) {
        ChessBoard copy = new ChessBoard();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = original.getPiece(pos);
                if (piece != null) {
                    copy.addPiece(pos, new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
                }
            }
        }
        return copy;
    }

    // Helper: Apply move to a board
    private void simulateMove(ChessBoard tempBoard, ChessMove move) {
        ChessPiece movingPiece = tempBoard.getPiece(move.getStartPosition());
        tempBoard.addPiece(move.getEndPosition(), new ChessPiece(
                movingPiece.getTeamColor(),
                move.getPromotionPiece() != null ? move.getPromotionPiece() : movingPiece.getPieceType()));
        tempBoard.addPiece(move.getStartPosition(), null);
    }

    // Helper: Locate the king of a team
    private ChessPosition findKingPosition(TeamColor teamColor, ChessBoard boardToSearch) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = boardToSearch.getPiece(pos);
                if (piece != null &&
                        piece.getTeamColor() == teamColor &&
                        piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return pos;
                }
            }
        }
        return null;
    }

    // Helper: Check if a position is under attack
    private boolean isPositionUnderAttack(ChessPosition position, TeamColor defender, ChessBoard boardToSearch) {
        TeamColor attacker = (defender == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = boardToSearch.getPiece(pos);
                if (piece != null && piece.getTeamColor() == attacker) {
                    for (ChessMove move : piece.pieceMoves(boardToSearch, pos)) {
                        if (move.getEndPosition().equals(position)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // Helper: Get all legal moves for a team
    private List<ChessMove> getAllLegalMoves(TeamColor teamColor) {
        List<ChessMove> allMoves = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> valid = validMoves(pos);
                    if (valid != null) {
                        allMoves.addAll(valid);
                    }
                }
            }
        }
        return allMoves;
    }
}
