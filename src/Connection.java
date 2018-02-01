import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Connection implements Runnable, GameConstraints{
    private Socket player1, player2;
    private String[][] cell = new String[ROWS][COLUMNS];
    private int sessionNumber;

    public Connection(Socket player1, Socket player2, int sessionNumber) {

        this.player1 = player1;
        this.player2 = player2;
        this.sessionNumber = sessionNumber;

        for (int x = 0; x < ROWS; x++) {
            for (int y = 0; y < COLUMNS; y++) {
                cell[x][y] = " ";
            }
        }
    }

    public void run() {
        try (DataInputStream inputStreamPlayer1 = new DataInputStream(player1.getInputStream());
             DataOutputStream outputStreamPlayer1 = new DataOutputStream(player1.getOutputStream());
             DataInputStream inputStreamPlayer2 = new DataInputStream(player2.getInputStream());
             DataOutputStream outputStreamPlayer2 = new DataOutputStream(player2.getOutputStream())) {

             outputStreamPlayer1.writeInt(1);

             while (true) {
                 int row = inputStreamPlayer1.readInt();
                 int column = inputStreamPlayer1.readInt();
                 String token = "r";
                 cell[row][column] = token;

                 if (isWon(row, column, token)) {
                     outputStreamPlayer1.writeInt(PLAYER1_WON);
                     outputStreamPlayer2.writeInt(PLAYER1_WON);
                     sendMove(outputStreamPlayer2, row, column);
                     break;
                 }
                 else if (isFull()) {
                     outputStreamPlayer1.writeInt(DRAW);
                     outputStreamPlayer2.writeInt(DRAW);
                     sendMove(outputStreamPlayer2, row, column);
                     break;
                 }
                 else {
                     outputStreamPlayer2.writeInt(CONTINUE);
                     sendMove(outputStreamPlayer2, row, column);
                 }

                 row = inputStreamPlayer2.readInt();
                 column = inputStreamPlayer2.readInt();
                 token = "y";
                 cell[row][column] = token;

                 if (isWon(row, column, token)) {
                     outputStreamPlayer1.writeInt(PLAYER2_WON);
                     outputStreamPlayer2.writeInt(PLAYER2_WON);
                     sendMove(outputStreamPlayer1, row, column);
                     break;
                 }
                 else {
                     outputStreamPlayer1.writeInt(CONTINUE);
                     sendMove(outputStreamPlayer1, row, column);
                 }
             }
        } catch (SocketException e) {}
          catch (IOException e) {}


    }

    private void sendMove(DataOutputStream outputStream, int row, int column) throws IOException {
        outputStream.writeInt(row);
        outputStream.writeInt(column);
    }

    private boolean isWon(int row, int column, String token){
        System.out.println("Session No : " + sessionNumber);
        for (int x = 0; x < ROWS; x++){
            for (int y = 0; y < COLUMNS; y++){
                System.out.print(cell[x][y]);
            }
            System.out.println();
        }

        //HORIZONTAL
        for (int x = 0; x < ROWS; x++){
            for (int y = 0; y < 4; y++){
                if (cell[x][y] == token && cell[x][y + 1] == token && cell[x][y + 2] == token && cell[x][y + 3] == token) {
                    return true;
                }
            }
        }

        //VERTICAL
        for (int x = 0; x < 3; x++){
            for (int y = 0; y < COLUMNS; y++){
                if (cell[x][y] == token && cell[x + 1][y] == token && cell[x + 2][y] == token && cell[x + 3][y] == token) {
                    return true;
                }
            }
        }

        //LEFT DIAGONAL [\]
        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 4; y++){
                if (cell[x][y] == token && cell[x + 1][y + 1] == token && cell[x + 2][y + 2] == token && cell[x + 3][y + 3] == token) {
                    return true;
                }
            }
        }

        //RIGHT DIAGONAL [/]
        for (int x = 0; x < 3; x++){
            for (int y = 0; y < COLUMNS; y++){
                if (cell[x][y] == token && cell[x + 1][y - 1] == token && cell[x + 2][y - 2] == token && cell[x + 3][y - 3] == token) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFull() {
        for (int x = 0; x < ROWS; x++) {
            for (int y = 0; y< COLUMNS; y++) {
                if (cell[x][y] == " ") {
                    return false;
                }
            }
        }
        return true;
    }
}
