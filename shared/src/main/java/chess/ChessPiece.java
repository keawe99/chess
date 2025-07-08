package chess;

import java.util.*;

public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    public ChessGame.TeamColor getTeamColor() { return pieceColor; }
    public PieceType getPieceType() { return type; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessPiece p)) return false;
        return p.pieceColor == pieceColor && p.type == type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return pieceColor + " " + type;
    }

    public enum PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition from, ChessGame game) {
        return pieceMoves(board, from, game, true); // default to including castling
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition from, ChessGame game, boolean includeCastling) {
        List<ChessMove> moves = new ArrayList<>();
        int r = from.getRow(), c = from.getColumn();

        switch(type) {
            case KING -> {
                for(int dr=-1; dr<=1; dr++)
                    for(int dc=-1; dc<=1; dc++) {
                        if (dr==0 && dc==0) continue;
                        int rr = r+dr, cc = c+dc;
                        if (inBounds(rr,cc))
                            addIfValid(board, from, new ChessPosition(rr,cc), moves);
                    }
                if (includeCastling) {
                    addCastling(board, from, moves, game);
                }
            }
            case QUEEN -> addSliding(board, from, moves,
                    new int[][]{{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}});
            case ROOK -> addSliding(board, from, moves,
                    new int[][]{{1,0},{-1,0},{0,1},{0,-1}});
            case BISHOP -> addSliding(board, from, moves,
                    new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}});
            case KNIGHT -> {
                for(int[] d : new int[][]{{2,1},{1,2},{-1,2},{-2,1},{-2,-1},{-1,-2},{1,-2},{2,-1}}) {
                    int rr = r+d[0], cc = c+d[1];
                    if (inBounds(rr,cc))
                        addIfValid(board, from, new ChessPosition(rr,cc), moves);
                }
            }
            case PAWN -> {
                int dir = pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1;
                int startR = pieceColor == ChessGame.TeamColor.WHITE ? 2 : 7;
                int endR = pieceColor == ChessGame.TeamColor.WHITE ? 8 : 1;

                // Forward
                if (inBounds(r+dir,c) && board.getPiece(new ChessPosition(r+dir,c)) == null) {
                    addPawnMove(from, r+dir, c, endR, moves);
                    if (r == startR && board.getPiece(new ChessPosition(r+2*dir,c)) == null)
                        moves.add(new ChessMove(from, new ChessPosition(r+2*dir,c), null));
                }
                // Captures
                for(int dc : new int[]{-1,1}) {
                    int cc2 = c+dc;
                    int rr2 = r+dir;
                    ChessPosition capP = new ChessPosition(rr2, cc2);
                    if (inBounds(rr2,cc2) && board.getPiece(capP) != null
                            && board.getPiece(capP).getTeamColor() != pieceColor) {
                        addPawnMove(from, rr2, cc2, endR, moves);
                    }
                    // En Passant
                    ChessPosition ep = game.getEnPassantTarget();
                    if (ep != null && ep.getRow() == rr2 && ep.getColumn() == cc2) {
                        moves.add(new ChessMove(from, ep, null));
                    }
                }
            }
        }
        return moves;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition from) {
        ChessGame dummyGame = new ChessGame();
        return pieceMoves(board, from, dummyGame, true);
    }

    private void addSliding(ChessBoard b, ChessPosition f, List<ChessMove> m, int[][] dirs) {
        for (int[] d : dirs) {
            int rr = f.getRow(), cc = f.getColumn();
            while (true) {
                rr += d[0]; cc += d[1];
                if (!inBounds(rr,cc)) break;
                ChessPosition to = new ChessPosition(rr, cc);
                ChessPiece tgt = b.getPiece(to);
                if (tgt == null) m.add(new ChessMove(f,to,null));
                else {
                    if (tgt.getTeamColor() != pieceColor)
                        m.add(new ChessMove(f,to,null));
                    break;
                }
            }
        }
    }

    private void addIfValid(ChessBoard b, ChessPosition f, ChessPosition t, List<ChessMove> m) {
        ChessPiece tgt = b.getPiece(t);
        if (tgt == null || tgt.getTeamColor() != pieceColor)
            m.add(new ChessMove(f,t,null));
    }

    private void addPawnMove(ChessPosition f, int rr, int cc, int endR, List<ChessMove> m) {
        ChessPosition t = new ChessPosition(rr, cc);
        if (rr == endR) {
            for (PieceType p : new PieceType[]{PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT})
                m.add(new ChessMove(f, t, p));
        } else {
            m.add(new ChessMove(f, t, null));
        }
    }

    private void addCastling(ChessBoard b, ChessPosition f, List<ChessMove> m, ChessGame game) {
        ChessGame.TeamColor col = pieceColor;

        if ((col == ChessGame.TeamColor.WHITE && !f.equals(new ChessPosition(1, 5))) ||
                (col == ChessGame.TeamColor.BLACK && !f.equals(new ChessPosition(8, 5)))) return;

        if (game.hasKingMoved(col) || game.isInCheck(col)) return;

        int row = f.getRow();

        // Kingside castling
        if (!game.hasKingsideRookMoved(col)
                && b.getPiece(new ChessPosition(row, 6)) == null
                && b.getPiece(new ChessPosition(row, 7)) == null
                && !game.isUnderAttack(new ChessPosition(row, 5), col, b)
                && !game.isUnderAttack(new ChessPosition(row, 6), col, b)
                && !game.isUnderAttack(new ChessPosition(row, 7), col, b)) {
            m.add(new ChessMove(f, new ChessPosition(row, 7), null));
        }

        // Queenside castling
        if (!game.hasQueensideRookMoved(col)
                && b.getPiece(new ChessPosition(row, 4)) == null
                && b.getPiece(new ChessPosition(row, 3)) == null
                && b.getPiece(new ChessPosition(row, 2)) == null
                && !game.isUnderAttack(new ChessPosition(row, 5), col, b)
                && !game.isUnderAttack(new ChessPosition(row, 4), col, b)
                && !game.isUnderAttack(new ChessPosition(row, 3), col, b)) {
            m.add(new ChessMove(f, new ChessPosition(row, 3), null));
        }
    }

    private boolean inBounds(int rr, int cc) {
        return rr >= 1 && rr <= 8 && cc >= 1 && cc <= 8;
    }
}
