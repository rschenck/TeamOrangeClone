import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

public class Fantastasize
{	
	public static int numNA = 100;
	public static int[] totNAL = new int[numNA];
	public static int[] TCR = new int[numNA];
	public static int TKillRate = 1;
	int tmax=10*24+1;

	public static void main (String[] args)
	{
		//set initial condition for TCR
        for (int i=0;i<numNA;i++){
            TCR[i]=1;
        }

		List<Cell> cellList = new ArrayList<Cell>();

		//add new cell
		cellList.add(new Cell());
		Cell cell = cellList.get(0);
		cell.setInititalCondition();

		totNAL[0]=1;
		int cellListSize;
        int tmax=10*24+1;


        for (int t=1; t< tmax; t++){//time loop

			cellListSize=cellList.size();
			for (int i=0; i<cellListSize; i++){//cell loop
				cellList.get(i).advance();
				if (cellList.get(i).isMature()){
					cellList.get(i).resetCCCycler(); // Reset Cell Cycle Cycler
                    cellList.get(i).proteomemutate(); // Acquire neoantigens
                    cellList.get(i).phenomute(); // Mutate phenotype
					Cell newC = new Cell();
					cellList.add(newC);
				}
			}
            if (t%1==0){//print population every day
            	int sumNA = findTotalArray(totNAL);
            	int sumTNAL = findTotalArray(TCR);
               System.out.println(t+","+cellList.size()+","+sumNA+","+sumTNAL);

                //write TCR to file
                String str="";//create an empty string
                for(int i=0; i<TCR.length; i++){
                    str += " "+TCR[i];//record all of TCR to file
                }
                str += "\n";//creates a new line;
                write("TCR.txt",str); //write string to file

                //write NAL to file
                str="";//create an empty string
                for(int i=0; i<totNAL.length; i++){
                    str += " "+totNAL[i];//record all of TCR to file
                }
                str += "\n";//creates a new line;
                write("totNAL.txt",str); //write string to file
            }

		}
	}

	public static int findTotalArray(int[] array){
		int sum=0;
		for(int i=0;i<array.length;i++){
			sum+=array[i];
		}
		return sum;
	}

	public static void write (String filename, String data)
	{
		try{//exception handling, print a message if doesn't succeed
    		FileWriter fw = new FileWriter(filename,true); //the true will append the new data
    		fw.write(data);//appends the string to the file
   	 		fw.close();
		}
		catch(IOException ioe){
    		System.err.println("IOException: " + ioe.getMessage());
		}
	}


}
