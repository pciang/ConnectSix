package com.connectsix;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends Activity {
	private final int MAX_ROW = 20;
	private final int MAX_COL = 15;
	private final char player1Piece = 'X';
	private final char player2Piece = 'O';
	private final int OBJECTIVE = 6;
	private LinearLayout mainContainer;
	private static int nextTag = 0;
	private final int[] dr = {-1, -1, -1, 0};
	private final int[] dc = {-1, 0, 1, 1};
	private boolean gameOver;
	private TextView statusField;
	
	private char[][] states;
	private Button[][] buttons;
	private int movesLeft;
	private boolean player1Turn;
	
	private ArrayList<Integer> lastR, lastC;
	
	public void undoLastMove(View view){
		if(lastR.isEmpty())
			return;
		
		int row = lastR.get(lastR.size() - 1);
		int col = lastC.get(lastC.size() - 1);
		
		if(gameOver){
			gameOver = false;
			movesLeft++;
			statusField.setText(player1Turn ? R.string.player1Turn : R.string.player2Turn);
		}
		else{
			if(movesLeft == 2){
				movesLeft = 1;
				player1Turn ^= true;
			}
			else if(movesLeft == 1){
				movesLeft++;
			}
			statusField.setText(player1Turn ? R.string.player1Turn : R.string.player2Turn);
		}
		
		lastR.remove(lastR.size() - 1);
		lastC.remove(lastC.size() - 1);
		
		states[row][col] = ' ';
		buttons[row][col].setText(R.string.emptyGrid);
		buttons[row][col].setBackgroundResource(R.drawable.empty_grid);
	}
	
	public void resetGame(View view){
		gameOver = false;
		player1Turn = true;
		movesLeft = 1;
		statusField.setText(R.string.player1Turn);
		for(int i = 0; i < MAX_ROW; ++i)
			for(int j = 0; j < MAX_COL; ++j){
				states[i][j] = ' ';
				buttons[i][j].setText(R.string.emptyGrid);
				buttons[i][j].setBackgroundResource(R.drawable.empty_grid);
			}
		
		lastR = new ArrayList<Integer>();
		lastC = new ArrayList<Integer>();
	}
	
	public void checkTurn(){
		if(movesLeft <= 0){
			movesLeft = 2;
			player1Turn ^= true;
			statusField.setText(player1Turn ? R.string.player1Turn : R.string.player2Turn);
		}
	}
	
	public int countPiece(int row, int col){
		int count = 1;
		
		for(int dir = 0; dir < 4; ++dir){
			int temp = 1;
			
			for(int k = 1, r = row + k * dr[dir], c = col + k * dc[dir];
				0 <= r && r < MAX_ROW && 0 <= c && c < MAX_COL;){
				if(states[r][c] == states[row][col])
					temp++;
				else
					break;
				
				r = row + (++k) * dr[dir];
				c = col + k * dc[dir];
			}
			
			for(int k = -1, r = row + k * dr[dir], c = col + k * dc[dir];
				0 <= r && r < MAX_ROW && 0 <= c && c < MAX_COL;){
				if(states[r][c] == states[row][col])
					temp++;
				else
					break;
				
				r = row + (--k) * dr[dir];
				c = col + k * dc[dir];
			}
			
			count = temp > count ? temp : count;
		}
		
		// Log.d("countPiece(int, int)", "Result: " + Integer.toString(count));
		return count;
	}
	
	public void placePiece(int row, int col){
		states[row][col] = player1Turn ? player1Piece : player2Piece;
		buttons[row][col].setText(player1Turn ? R.string.player1Piece : R.string.player2Piece);
		buttons[row][col].setBackgroundResource(player1Turn ? R.drawable.player1_piece : R.drawable.player2_piece);
		
		// UNDO FEATURE
		lastR.add(row);
		lastC.add(col);
		
		movesLeft--;
	}
	
	public void click(View view){
		if(gameOver)
			return;
		
		Button button = (Button) view;
		int tag = Integer.parseInt(button.getTag().toString());
		int row = tag / MAX_COL;
		int col = tag % MAX_COL;
		
		if(states[row][col] == ' '){
			placePiece(row, col);
			
			int counter = countPiece(row, col);
			if(counter >= OBJECTIVE){
				gameOver = true;
				statusField.setText(player1Turn ? R.string.player1Win : R.string.player2Win);
				
				// The game ends here
				return;
			}
			
			checkTurn();
		}
	}
	
	private void dfs(View view){
		if(view instanceof Button){
			Button button = (Button) view;
			button.setTag(nextTag);
			
			int row = nextTag / MAX_COL;
			int col = nextTag % MAX_COL;
			buttons[row][col] = button;
			
			
			button.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view)
				{
					click(view);
				}
			});
			
			nextTag++;
			return;
		}
		
		for(int i = 0, size = ((ViewGroup) view).getChildCount(); i < size; ++i)		{
			View child = ((ViewGroup) view).getChildAt(i);
			dfs(child);
		}
	}
	
	public void showHelp(View view){
		new AlertDialog.Builder(this)
		.setTitle("Connect Six")
		.setMessage("The objective of this game is to connect six of your pieces in a row (either horizontally, vertically, or diagonally). Each player takes turn to place two of their pieces. Except for the first turn, where the first player may only place one piece.")
		.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				
			}
		})
		.setIcon(R.drawable.connect6_icon_small)
		.show();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        nextTag = 0;
        
        // Initialization
        mainContainer = (LinearLayout) findViewById(R.id.main_container);
        statusField = (TextView) findViewById(R.id.status_field);
        states = new char[MAX_ROW][MAX_COL];
        buttons = new Button[MAX_ROW][MAX_COL];
        dfs(mainContainer);
        
        lastR = new ArrayList<Integer>();
        lastC = new ArrayList<Integer>();
        
        resetGame(null);
        showHelp(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {}
}
