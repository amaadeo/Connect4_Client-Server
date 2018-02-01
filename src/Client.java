import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.io.*;
import java.net.*;

public class Client extends JApplet implements Runnable, GameConstraints {
    private boolean myTurn = false;
    private String myToken = " ";
    private String otherToken = " ";
    private Cell[][] cell = new Cell[ROWS][COLUMNS];
    private JLabel title = new JLabel();
    private JLabel status = new JLabel();
    private int rowSelected, columnSelected;
    private boolean continueToPlay = true;
    private boolean waitingForOpponentTurn = true;
    private boolean isStandAlone = false;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    Socket clientSocket;

    public boolean initClientGame(){
		boolean flag = connectToServer(); 
        if (flag) {
            JPanel panel = new JPanel();
            panel.setBackground(Color.GRAY);
            panel.setLayout(new GridLayout(ROWS, COLUMNS, 0, 0));

            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLUMNS; j++)
                    panel.add(cell[i][j] = new Cell(i, j, cell));
            }
            panel.setBorder(new EmptyBorder(20,50,20,50));
            title.setPreferredSize(new Dimension(640, 70));
            title.setHorizontalAlignment(JLabel.CENTER);
            title.setVerticalAlignment(SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 30));
            title.setBorder(new LineBorder(Color.BLACK, 1));
            status.setPreferredSize(new Dimension(640, 50));
            status.setBorder(new LineBorder(Color.BLACK, 1));
            status.setFont(new Font("Arial", Font.CENTER_BASELINE, 25));
            status.setHorizontalAlignment(SwingConstants.CENTER);
            status.setVerticalAlignment(SwingConstants.CENTER);

            add(title, BorderLayout.NORTH);
            add(panel, BorderLayout.CENTER);
            add(status, BorderLayout.SOUTH);
        }
        else {
            System.out.println("Błąd połączenia z serwerem...\nUpewnij się, że serwer jest włączony.");
        }
		return flag;
    }

    private boolean connectToServer() {
        boolean correct = false;
        try{
            clientSocket = new Socket(ServerConfiguration.getHost(), ServerConfiguration.getPort());
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            correct = true;
            Thread thread = new Thread(this);
            thread.start();
        } catch (UnknownHostException e) {}
          catch (IOException e) {}

        return correct;
    }

    public void run() {
        try{
            System.out.println("Zostales polaczony z serwerm!");

            int player = inputStream.readInt();

            if (player == PLAYER1) {
                myToken = "r";
                otherToken = "y";
                title.setText("Twój kolor: CZERWONY");
                status.setText("Oczekiwanie na dołączenie drugiego gracza...");

                inputStream.readInt();
                status.setText("Przeciwnik  dołączył. Ty zaczynacz!");

                myTurn = true;
            }
            else if (player == PLAYER2) {
                myToken = "y";
                otherToken = "r";

                title.setText("Twój kolor: ŻÓŁTY");
                status.setText("Oczekiwanie na ruch drugiego gracza...");
            }

            while (continueToPlay) {
                if (player == PLAYER1){
                    waitForPlayerMove();
                    sendMoveToServer();
                    receiveAnswerFromServer();
                }
                else if (player == PLAYER2) {
                    receiveAnswerFromServer();
                    waitForPlayerMove();
                    sendMoveToServer();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMoveToServer() throws IOException {
        outputStream.writeInt(rowSelected);
        outputStream.writeInt(columnSelected);
    }

    private void waitForPlayerMove() throws InterruptedException {
        while (waitingForOpponentTurn) {
            Thread.sleep(100);
        }
        waitingForOpponentTurn = true;
    }

    private void receiveAnswerFromServer() throws IOException {
        int gameStatus = inputStream.readInt();

        if (gameStatus == PLAYER1_WON){
            continueToPlay = false;
            if (myToken.equals("r")){
                status.setText("WYGRANA!");
            }
            else if (myToken.equals("y")) {
                status.setText("PRZEGRANA!");
                receiveMove();
            }
        }
        else if (gameStatus == PLAYER2_WON){
            continueToPlay = false;
            if (myToken.equals("y")){
                status.setText("WYGRANA!");
            }
            else if (myToken.equals("r")) {
                status.setText("PRZEGRANA!");
                receiveMove();
            }
        }
        else if (gameStatus == DRAW){
            continueToPlay = false;
            status.setText("REMIS!");
            if (myToken.equals("y")){
                receiveMove();
            }
        }
        else {
            receiveMove();
            status.setText("Twój ruch");
            myTurn = true;
        }
    }

    private void receiveMove() throws IOException {
        int row = inputStream.readInt();
        int column = inputStream.readInt();
        cell[row][column].setToken(otherToken);
    }

    public class Cell extends JPanel {
        private int row, column;
        private Cell[][] cell;

        private String token = " ";

        public Cell(int row, int column, Cell[][] cell) {
            this.row = row;
            this.column = column;
            this.cell = cell;
            setBackground(Color.GRAY);
            setBackground(new Color(40,0,210));
            addMouseListener(new ClickListener());
        }

        public String getToken() {
            return token;
        }

        public void setToken(String c) {
            token = c;
            repaint();
        }

        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            graphics.setColor(Color.WHITE);
            graphics.fillOval(18,9, getWidth() - 35, getHeight() - 15);

            if (token.equals("r")) {
                graphics.setColor(Color.RED);
                graphics.fillOval(18,9, getWidth() - 35, getHeight() - 15);
            }
            else if (token.equals("y")) {
                graphics.setColor(Color.YELLOW);
                graphics.fillOval(18,9, getWidth() - 35, getHeight() - 15);
            }
        }

        private class ClickListener extends MouseAdapter {
            public void mouseClicked(MouseEvent event){
                int r = -1;
                for (int x = 5; x >= 0; x--){
                    if (cell[x][column].getToken().equals(" ")) {
                        r = x;
                        break;
                    }
                }

                if ((r != -1) && myTurn) {
                    cell[r][column].setToken(myToken);
                    myTurn = false;
                    rowSelected = r;
                    columnSelected = column;
                    status.setText("Oczekiwanie na ruch drugiego gracza...");
                    waitingForOpponentTurn = false;
                }
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Connect4");

        Client client = new Client();
        client.isStandAlone = true;

        frame.getContentPane().add(client, BorderLayout.CENTER);

        if (client.initClientGame()){
			client.start();

			frame.setSize(900, 750);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
			frame.setResizable(false);
		}
    }
}
