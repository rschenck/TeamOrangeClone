import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

class Cell{   
    static int[] maxmin={10,50};//range of cell cycle values
    static int diststart=50; //median cell cycle time
    static int stddev = 3; //std dev of cell cycle times
    static Random generator = new Random(); //random number generator
    static int neoMutRate = 100;//mut 1/1000
    static int mutRate = 1000;


    int cellCycleLength;
	int positionInCellCycle=0;
	boolean brafSens = true;
	int[] neoAntigenLoad = new int[CellSimulationRndCC.numNA];
    
    private static int getCCLength(){//finds the cell cycle length from a Gaussian distribution
    	int tempCCL = (int) (diststart+stddev*generator.nextGaussian());
    	while(tempCCL>maxmin[1] && tempCCL<maxmin[0]){
    		tempCCL = (int) (diststart+stddev*generator.nextGaussian());
    	}
        return tempCCL;
    }

    private int changeCCLength(){//finds the cell cycle length from a Gaussian distribution
    	int tempCCL = (int) (this.cellCycleLength+stddev*generator.nextGaussian());
    	while(tempCCL>maxmin[1] && tempCCL<maxmin[0]){
    		tempCCL = (int) (this.cellCycleLength+stddev*generator.nextGaussian());
    	}
        return tempCCL;
    }
    
	public Cell(){//constructor
        this.cellCycleLength=getCCLength();
        this.neoAntigenLoad=getNeoAntigenLoad();
	}
    
	public void advance() {
		positionInCellCycle++;}
	
	public boolean isMature(){
		return positionInCellCycle>=this.cellCycleLength; }
	
	public int getPositionInCellCycle(){
		return this.positionInCellCycle; }

	public int getCellCycleLength(){
		return this.cellCycleLength; }

	public int[] getNeoAntigenLoad(){
		return this.neoAntigenLoad; }
	
	public void reset(){
		this.positionInCellCycle=0;
		if(generator.nextInt(neoMutRate)==0){
			int newNA = generator.nextInt(CellSimulationRndCC.numNA);
			this.neoAntigenLoad[newNA]+=1;
			CellSimulationRndCC.totNAL[newNA]+=1;
			CellSimulationRndCC.TCR[newNA]+=1;
		}
		if(generator.nextInt(mutRate)==0){
			this.cellCycleLength=changeCCLength();
		}
	}
};

public class CellSimulationRndCC
{	
	public static int numNA = 100;
	public static int[] totNAL = new int[numNA];
	public static int[] TCR = new int[numNA];
	public static int tRate = 1;

	public static void main (String[] args)
	{
		int tmax=10*24+1;
		List<Cell> cellList = new ArrayList<Cell>();
		cellList.add(new Cell());
		Cell cell = cellList.get(0);
		cell.neoAntigenLoad[0]=1;
		totNAL[0]=1;
		int cellListSize;
		for (int i=0;i<numNA;i++){
			TCR[i]=1;
		}

		for (int t=1; t<tmax; t++){//time loop
			cellListSize=cellList.size();
			for (int i=0; i<cellListSize; i++){//cell loop
				cellList.get(i).advance();
				if (cellList.get(i).isMature()){
					cellList.get(i).reset();
					Cell newC = new Cell();
					cellList.add(newC);
				}
			}
            if (t%24==0){//print population every day
            	int sumNA = findTotalArray(totNAL);
            	int sumTNAL = findTotalArray(TCR);
               System.out.println(t+","+cellList.size()+","+sumNA+","+sumTNAL);
            }
            String str="";//create an empty string
			for(int i=0; i<cellListSize; i++){		 
				str += " "+cellList.get(i).getCellCycleLength();//add the cc of each cell to string
			}
			str += "\n";//creates a new line;
			write("cellCycleLength.txt",str); //write string to file
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
