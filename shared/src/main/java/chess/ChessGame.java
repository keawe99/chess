package chess;

import java.util.*;

public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;

    // Move tracking for castling
    private boolean whiteKingMoved = false;
    private boolean whiteKingsideRookMoved = false;
    private boolean whiteQueensideRookMoved = false;
    private boolean blackKingMoved = false;
    private boolean blackKingsideRookMoved = false;
    private boolean blackQueensideRookMoved = false;

    // Tracking en passant
    private ChessPosition enPassantTarget = null;
    private ChessPosition chessPosition;
    private TeamColor col;
    private ChessBoard b;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    public boolean isUnderAttack(ChessPosition pos, TeamColor color, ChessBoard board) {
        return isPositionUnderAttack(pos, color, board);
    }

    public enum TeamColor {
        WHITE, BLACK
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
        // Reset castling / en passant state
        whiteKingMoved = whiteKingsideRookMoved = whiteQueensideRookMoved =
                blackKingMoved = blackKingsideRookMoved = blackQueensideRookMoved = false;
        enPassantTarget = null;
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);

        // ✅ Always return a non-null collection
        if (piece == null) {
            return Collections.emptyList();
        }

        // Get pseudo-legal moves (may include illegal moves like exposing king to check)
        Collection<ChessMove> pseudo = piece.pieceMoves(board, startPosition, this);
        List<ChessMove> legal = new ArrayList<>();

        for (ChessMove move : pseudo) {
            // Clone board to simulate move
            ChessBoard simulated = cloneBoard(board);
            simulateMove(simulated, move);

            // Find the king position after the move
            ChessPosition kingPos = findKingPosition(piece.getTeamColor(), simulated);

            // Only allow move if it doesn't leave king in check
            if (kingPos != null && !isPositionUnderAttack(kingPos, piece.getTeamColor(), simulated)) {
                legal.add(move);
            }
        }

        return legal;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Invalid move or not your piece");
        }

        Collection<ChessMove> legal = validMoves(move.getStartPosition());
        if (legal == null || !legal.contains(move)) {
            throw new InvalidMoveException("Move is not valid");
        }

        ChessPosition s = move.getStartPosition();
        ChessPosition e = move.getEndPosition();

        // Handle en passant capture
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN
                && s.getColumn() != e.getColumn()
                && board.getPiece(e) == null) {
            int dir = (piece.getTeamColor() == TeamColor.WHITE) ? -1 : 1;
            ChessPosition captured = new ChessPosition(e.getRow() + dir, e.getColumn());
            board.addPiece(captured, null);
        }

        // Make the move / Promotion
        board.addPiece(e, new ChessPiece(piece.getTeamColor(),
                move.getPromotionPiece() != null ? move.getPromotionPiece() : piece.getPieceType()));
        board.addPiece(s, null);

        // Handle castling rook reposition
        if (piece.getPieceType() == ChessPiece.PieceType.KING
                && Math.abs(e.getColumn() - s.getColumn()) == 2) {
            int row = s.getRow();
            if (e.getColumn() == 7) {
                // King-side castling
                ChessPosition rStart = new ChessPosition(row, 8);
                ChessPosition rEnd = new ChessPosition(row, 6);
                board.addPiece(rEnd, board.getPiece(rStart));
                board.addPiece(rStart, null);
            } else {
                // Queen-side castling
                ChessPosition rStart = new ChessPosition(row, 1);
                ChessPosition rEnd = new ChessPosition(row, 4);
                board.addPiece(rEnd, board.getPiece(rStart));
                board.addPiece(rStart, null);
            }
        }

        // Track en passant state for next move
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN
                && Math.abs(e.getRow() - s.getRow()) == 2) {
            int targetRow = (s.getRow() + e.getRow()) / 2;
            enPassantTarget = new ChessPosition(targetRow, s.getColumn());
        } else {
            enPassantTarget = null;
        }

        // Track king and rook first-move flags
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (teamTurn == TeamColor.WHITE) whiteKingMoved = true;
            else blackKingMoved = true;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            boolean white = teamTurn == TeamColor.WHITE;
            if (white) {
                if (s.getRow() == 1 && s.getColumn() == 1) whiteQueensideRookMoved = true;
                if (s.getRow() == 1 && s.getColumn() == 8) whiteKingsideRookMoved = true;
            } else {
                if (s.getRow() == 8 && s.getColumn() == 1) blackQueensideRookMoved = true;
                if (s.getRow() == 8 && s.getColumn() == 8) blackKingsideRookMoved = true;
            }
        }

        teamTurn = (teamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kp = findKingPosition(teamColor, board);
        return kp != null && isPositionUnderAttack(kp, teamColor, board);
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && getAllLegalMoves(teamColor).isEmpty();
    }

    public boolean isInStalemate(TeamColor team) {
        if (isInCheck(team)) return false;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == team) {
                    Collection<ChessMove> moves = validMoves(pos);
                    if (!moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // ---------------- Helper Functions ----------------

    private ChessBoard cloneBoard(ChessBoard orig) {
        ChessBoard copy = new ChessBoard();
        for (int r = 1; r <= 8; r++)
            for (int c = 1; c <= 8; c++) {
                ChessPosition p = new ChessPosition(r, c);
                ChessPiece pc = orig.getPiece(p);
                if (pc != null) copy.addPiece(p, new ChessPiece(pc.getTeamColor(), pc.getPieceType()));
            }
        return copy;
    }

    private void simulateMove(ChessBoard b, ChessMove m) {
        ChessPiece pc = b.getPiece(m.getStartPosition());
        b.addPiece(m.getEndPosition(), new ChessPiece(pc.getTeamColor(),
                m.getPromotionPiece() != null ? m.getPromotionPiece() : pc.getPieceType()));
        b.addPiece(m.getStartPosition(), null);

        if (pc.getPieceType() == ChessPiece.PieceType.KING
                && Math.abs(m.getEndPosition().getColumn() - m.getStartPosition().getColumn()) == 2) {
            int row = m.getStartPosition().getRow();
            if (m.getEndPosition().getColumn() == 7) {
                ChessPosition rStart = new ChessPosition(row, 8);
                ChessPosition rEnd = new ChessPosition(row, 6);
                b.addPiece(rEnd, b.getPiece(rStart));
                b.addPiece(rStart, null);
            } else {
                ChessPosition rStart = new ChessPosition(row, 1);
                ChessPosition rEnd = new ChessPosition(row, 4);
                b.addPiece(rEnd, b.getPiece(rStart));
                b.addPiece(rStart, null);
            }
        }
    }

    private ChessPosition findKingPosition(TeamColor teamColor, ChessBoard brd) {
        for (int r = 1; r <= 8; r++)
            for (int c = 1; c <= 8; c++) {
                ChessPosition p = new ChessPosition(r, c);
                ChessPiece pc = brd.getPiece(p);
                if (pc != null && pc.getTeamColor() == teamColor
                        && pc.getPieceType() == ChessPiece.PieceType.KING)
                    return p;
            }
        return null;
    }

    private boolean isPositionUnderAttack(ChessPosition pos, TeamColor defender, ChessBoard brd) {
        TeamColor attacker = (defender == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);

        ChessGame dummyGame = new ChessGame();
        dummyGame.setBoard(brd);  // mimic game state

        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition check = new ChessPosition(r, c);
                ChessPiece pc = brd.getPiece(check);
                if (pc != null && pc.getTeamColor() == attacker) {
                    // ⚠️ Skip castling when checking for attack!
                    for (ChessMove m : pc.pieceMoves(brd, check, dummyGame, false)) {
                        if (m.getEndPosition().equals(pos)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private List<ChessMove> getAllLegalMoves(TeamColor teamColor) {
        List<ChessMove> all = new ArrayList<>();
        for (int r = 1; r <= 8; r++)
            for (int c = 1; c <= 8; c++) {
                ChessPosition p = new ChessPosition(r, c);
                ChessPiece pc = board.getPiece(p);
                if (pc != null && pc.getTeamColor() == teamColor) {
                    Collection<ChessMove> m = validMoves(p);
                    if (m != null) all.addAll(m);
                }
            }
        return all;
    }

    // Exposed to ChessPiece for castling/en passant support
    boolean hasKingMoved(TeamColor color) {
        return color == TeamColor.WHITE ? whiteKingMoved : blackKingMoved;
    }
    boolean hasKingsideRookMoved(TeamColor color) {
        return color == TeamColor.WHITE ? whiteKingsideRookMoved : blackKingsideRookMoved;
    }
    boolean hasQueensideRookMoved(TeamColor color) {
        return color == TeamColor.WHITE ? whiteQueensideRookMoved : blackQueensideRookMoved;
    }
    ChessPosition getEnPassantTarget() {
        return enPassantTarget;
    }

    // ---------------- equals() and hashCode() ----------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessGame other)) return false;

        return Objects.equals(this.board, other.board) &&
                this.teamTurn == other.teamTurn &&
                this.whiteKingMoved == other.whiteKingMoved &&
                this.whiteKingsideRookMoved == other.whiteKingsideRookMoved &&
                this.whiteQueensideRookMoved == other.whiteQueensideRookMoved &&
                this.blackKingMoved == other.blackKingMoved &&
                this.blackKingsideRookMoved == other.blackKingsideRookMoved &&
                this.blackQueensideRookMoved == other.blackQueensideRookMoved &&
                Objects.equals(this.enPassantTarget, other.enPassantTarget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                board,
                teamTurn,
                whiteKingMoved,
                whiteKingsideRookMoved,
                whiteQueensideRookMoved,
                blackKingMoved,
                blackKingsideRookMoved,
                blackQueensideRookMoved,
                enPassantTarget
        );
    }
}
