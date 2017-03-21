/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package numberboggle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 *
 * @author Juan Pablo
 */
public class NumberBoggle {
    
    /* Explanation of my implementation:
       First I generate the grid randomly. Then, for each cell of the grid,
       I choose it as a starting point for every possible chain. I begin 
       constructing the chains and I check every time if the sum to the gridArea
       is already reached.
    
       If the sum is not reached, I keep searching, making the chains longer.
       If I reach the sum, I have to check two things: 
          1. The chain length is at least "gridWidth - 1" long
          2. The chain (as a set) has not been previously taken
          If both things are true, then I save the chain, and I save a copy of 
          the chain as a set.
          Whether both things are true or not, I keep searching, making the
          chains longer.
    
       The program stops when the only chains left either can't get any longer,
       or have a sum greater than gridArea.
    
    
    
       NOTE: IF THERE ARE TWO OR MORE DIFFERENT CHAINS (WITH DIFFERENT CELLS) BUT 
       WITH THE SAME NUMBERS, IT IS ONLY DISPLAYED AS ONE CHAIN!!
       For example, if the  grid is:
        4    1    1  

        3    4    7  

        1    5    2  

        The chain
        4 + 4 + 1 = 9
        will only be displayed and counted as one, even though there are three
        different paths to get the chain. Still, it finds it, right?
    
        So, in reality, this program finds distinct sums.
    
      There is a bigger problem with repeated numbers. If a valid sum is
      2 + 7 + 0, and another one is 2 + 7 + 0 + 0, it checks if, as a set, they
      are the same (which they are), and so it discards one of them (that is bad D: )
    
      Besides that, it runs well and accurately.
    
    
    
       This program can handle grids of gridWidth x gridWidth in general.
       Algorithmically, I think it scales decently well, because even with all the
       recursiveness, I tried with up to gridWidth = 20 and it ran smoothly.
       
       Mathematically, however, the bigger the grid is, the more difficult it is
       to find valid chains. You can see that the program finds few or no chains
       for gridWidth >= 12, for example. Only in the rare case when the generated grid 
       is particularly "good" (has several small numbers that can adjust otherwise 
       near misses), then the program finds A LOT of valid chains.
    */
    
    private int gridWidth; //grid Width, can be changed
    private int[][] grid;
    private int gridArea;
    
    private ArrayList<ArrayList<Integer>> chains; // list of valid chains
    private ArrayList<HashSet<Integer>> chainsAsSets; // list of valid chains (stored as sets)

    public NumberBoggle(int gridWidth) {
        this.gridWidth = gridWidth;
        grid = new int[gridWidth][gridWidth];
        gridArea = gridWidth*gridWidth;
    }
    
    public void fillGrid(){
        Random r = new Random();
        for(int i=0;i<gridWidth;i++){
           for(int j=0;j<gridWidth;j++){
               grid[i][j]=r.nextInt(gridArea + 1); // no restriction of repetitions
               // there could be repeated numbers; no problem!
           } 
        }
    }
    
    public void printGrid(){
        for(int i=0;i<gridWidth;i++){
           for(int j=0;j<gridWidth;j++){
               if(grid[i][j] < 10){
                   System.out.print("  " + grid[i][j]+ "  ");
               }
               else if(grid[i][j] < 100){
                   System.out.print(" " + grid[i][j]+ "  ");
               }
               else{
                   System.out.print(grid[i][j]+ "  "); 
               }
           } 
           System.out.print("\n\n");
        }
    }
    
    //this method gets all the valid chains
    public void getChains(){
        chains = new ArrayList<ArrayList<Integer>>();
        chainsAsSets = new ArrayList<HashSet<Integer>>();
        
        for(int i=0;i<gridWidth;i++){
           for(int j=0;j<gridWidth;j++){
               Coordinate<Integer,Integer> actualPos = new Coordinate<Integer,Integer>(i,j);
               ArrayList<Coordinate<Integer,Integer>> actualChain = new ArrayList<Coordinate<Integer,Integer>>();
               actualChain.add(actualPos);
               searchForChains(actualPos,actualChain);
           } 
        }
        
    }
    
    // searchForChains receives the actual position and the actual chain up to this moment
    public void searchForChains(Coordinate<Integer,Integer> actualPos, ArrayList<Coordinate<Integer,Integer>> actualChain){
        if(sumChain(positionsToNumbers(actualChain)) < gridArea){
            continueToAdjacentCells(actualPos, actualChain); //continue searching
        }
        else if(sumChain(positionsToNumbers(actualChain)) == gridArea){
            if(actualChain.size() >= gridWidth - 1 && !chainsAsSets.contains(toSet(positionsToNumbers(actualChain)))){
                //check if it is the minimum length, and check if the chain is not already considered (in any order)
                //there can be a problem if there are different cells with repeated numbers.
                chains.add(positionsToNumbers(actualChain));
                chainsAsSets.add(toSet(positionsToNumbers(actualChain)));
            }
            continueToAdjacentCells(actualPos, actualChain);
        }
    }
    
    //transforms an array list to a set
    public HashSet<Integer> toSet(ArrayList<Integer> list){
        HashSet<Integer> set = new HashSet<Integer>();
        for(int i=0;i<list.size();i++){
            set.add(list.get(i));
        }
        return set;
    }
    
    // method that checks to which cells we can move (discard movement OUT of the grid, and discard movement to a previously visited cell)
    public void continueToAdjacentCells(Coordinate<Integer,Integer> actualPos, ArrayList<Coordinate<Integer,Integer>> actualChain){
        Coordinate<Integer,Integer> newPos;
        ArrayList<Coordinate<Integer,Integer>> newChain;
        if(actualPos.getFirst()>0){
            newPos = new Coordinate<Integer,Integer>(actualPos.getFirst()-1,actualPos.getSecond());
            if(!actualChain.contains(newPos)){
                newChain = new ArrayList<Coordinate<Integer,Integer>>();
                newChain = copy(actualChain);
                newChain.add(newPos);
                searchForChains(newPos,newChain);
            }
            if(actualPos.getSecond() > 0){
                newPos = new Coordinate<Integer,Integer>(actualPos.getFirst()-1,actualPos.getSecond()-1);
                if(!actualChain.contains(newPos)){
                    newChain = new ArrayList<Coordinate<Integer,Integer>>();
                    newChain = copy(actualChain);
                    newChain.add(newPos);
                    searchForChains(newPos,newChain);
                }
                
                newPos = new Coordinate<Integer,Integer>(actualPos.getFirst(),actualPos.getSecond()-1);
                if(!actualChain.contains(newPos)){
                    newChain = new ArrayList<Coordinate<Integer,Integer>>();
                    newChain = copy(actualChain);
                    newChain.add(newPos);
                    searchForChains(newPos,newChain);
                }
            }
            if(actualPos.getSecond() < gridWidth - 1){
                newPos = new Coordinate<Integer,Integer>(actualPos.getFirst()-1,actualPos.getSecond()+1);
                if(!actualChain.contains(newPos)){
                    newChain = new ArrayList<Coordinate<Integer,Integer>>();
                    newChain = copy(actualChain);
                    newChain.add(newPos);
                    searchForChains(newPos,newChain);
                }
                
                newPos = new Coordinate<Integer,Integer>(actualPos.getFirst(),actualPos.getSecond()+1);
                if(!actualChain.contains(newPos)){
                    newChain = new ArrayList<Coordinate<Integer,Integer>>();
                    newChain = copy(actualChain);
                    newChain.add(newPos);
                    searchForChains(newPos,newChain);
                }
            }
        }
        if(actualPos.getFirst() < gridWidth - 1){
            newPos = new Coordinate<Integer,Integer>(actualPos.getFirst()+1,actualPos.getSecond());
            if(!actualChain.contains(newPos)){
                newChain = new ArrayList<Coordinate<Integer,Integer>>();
                newChain = copy(actualChain);
                newChain.add(newPos);
                searchForChains(newPos,newChain);
            }
            if(actualPos.getSecond() > 0){
                newPos = new Coordinate<Integer,Integer>(actualPos.getFirst()+1,actualPos.getSecond()-1);
                if(!actualChain.contains(newPos)){
                    newChain = new ArrayList<Coordinate<Integer,Integer>>();
                    newChain = copy(actualChain);
                    newChain.add(newPos);
                    searchForChains(newPos,newChain);
                }
            }
            if(actualPos.getSecond() < gridWidth - 1){
                newPos = new Coordinate<Integer,Integer>(actualPos.getFirst()+1,actualPos.getSecond()+1);
                if(!actualChain.contains(newPos)){
                    newChain = new ArrayList<Coordinate<Integer,Integer>>();
                    newChain = copy(actualChain);
                    newChain.add(newPos);
                    searchForChains(newPos,newChain);
                }
            }
        }
    }
    
    // copies an arraylist
    public ArrayList<Coordinate<Integer,Integer>> copy(ArrayList<Coordinate<Integer,Integer>> ch){
        ArrayList<Coordinate<Integer,Integer>> newCh = new ArrayList<Coordinate<Integer,Integer>>();
        for(int i=0;i<ch.size();i++){
            newCh.add(ch.get(i));
        }
        return newCh;
    }
    
    // sums a chain to see if it is valid (it returns only the sum)
    public int sumChain(ArrayList<Integer> list){
        int sum = 0;
        for(int i=0;i<list.size();i++){
            sum += list.get(i);
        }
        return sum;
    }
    
    //transforms a list of coordinates to a list of integers of the grid
    public ArrayList<Integer> positionsToNumbers(ArrayList<Coordinate<Integer,Integer>> positions){
        ArrayList<Integer> listOfNumbers= new ArrayList<Integer>();
        for(int i=0;i<positions.size();i++){
            listOfNumbers.add(positionToNumber(positions.get(i)));
        }
        return listOfNumbers;
    }
    
    // transforms a coordinate to the corresponding grid integer
    public int positionToNumber(Coordinate<Integer,Integer> pos){
        return grid[pos.getFirst()][pos.getSecond()];
    }
    
    //prints the chains
    public void printChains(){
        for(ArrayList<Integer> chain : chains){
            for(int i=0;i<chain.size()-1;i++){
                System.out.print(chain.get(i)+ " + ");
            }
            System.out.print(chain.get(chain.size()-1)+ " = " + gridArea + "\n");
        }
        if(chains.size() == 1){
            System.out.println("One chain found.");
        }
        else{
            System.out.println(chains.size() + " chains found.");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Welcome to Number Boggle!\n");
        
        NumberBoggle boardGame = new NumberBoggle(3); // you can change the grid width here
        boardGame.fillGrid();
        
        System.out.println("The grid is:");
        boardGame.printGrid();
        
        boardGame.getChains();
        
        System.out.println("The valid chains found are:");
        boardGame.printChains();
        
        
    }
    
}
