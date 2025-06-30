package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board = new ChessPiece[8][8];  // row 0 = row 1, etc.


    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int row = position.getRow() - 1;
        int col = position.getColumn() - 1;
        board[row][col] = piece;
    }

    public ChessPiece getPiece(ChessPosition position) {
        int row = position.getRow() - 1;
        int col = position.getColumn() - 1;
        return board[row][col];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // Clear the board
        board = new ChessPiece[8][8];

        // Back row piece order
        ChessPiece.PieceType[] backRow = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };

        // Set up white pieces (row 1 and 2)
        for (int col = 0; col < 8; col++) {
            board[0][col] = new ChessPiece(ChessGame.TeamColor.WHITE, backRow[col]); // row 1
            board[1][col] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN); // row 2
        }

        // Set up black pieces (row 8 and 7)
        for (int col = 0; col < 8; col++) {
            board[7][col] = new ChessPiece(ChessGame.TeamColor.BLACK, backRow[col]); // row 8
            board[6][col] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN); // row 7
        }
    }
}
