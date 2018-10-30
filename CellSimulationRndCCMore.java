import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

class Cell
{
    static int diststart=19;
    static int[] maxmin={10,50};
    static int stddev = 5;
    static Random generator = new Random();
    
    private static int getCCLength()
    {
    	int tempCCL = (int) (diststart+stddev*generator.nextGaussian());
    	while(tempCCL>maxmin[1] && tempCCL<maxmin[0]){
    		tempCCL = (int) (diststart+stddev*generator.nextGaussian());
    	}
        return tempCCL;
    }

	int cellCycleLength=0;
	int positionInCellCycle=0;
    
	public Cell(){
        this.cellCycleLength=getCCLength();
		this.positionInCellCycle=0;}
    
	public void advance() {
		positionInCellCycle++; }
	
	public boolean isMature(){
		return positionInCellCycle>=this.cellCycleLength; }
	
	public int getPositionInCellCycle(){
		return this.positionInCellCycle; }

	public int getCellCycleLength(){
		return this.cellCycleLength; }
	
	public void reset(){
		this.positionInCellCycle=0; }
};

public class CellSimulationRndCC
{	

	public static void main (String[] args)
	{
		int tmax=10*24+1;
		List<Cell> cellList = new ArrayList<Cell>();
		cellList.add(new Cell());
		int cellListSize;
		//System.out.println(0+","+cellList.size());
		for (int t=1; t<tmax; t++){
			cellListSize=cellList.size();
			for (int i=0; i<cellListSize; i++){
				cellList.get(i).advance();
				if (cellList.get(i).isMature()){
					cellList.get(i).reset();
					Cell newC = new Cell();
					cellList.add(newC);
				}
			}
            if (t%24==0){
               // System.out.println(t+","+cellList.size());
            }
            String str="";
			for(int i=0; i<cellListSize; i++){		 
				str += " "+cellList.get(i).getCellCycleLength();
			}
			str += "\n";
			//System.out.println(str);
			write("cellCycleLength.txt",str);
		}
	}

	public static void write (String filename, String data)
	{
		try
		{
    		FileWriter fw = new FileWriter(filename,true); //the true will append the new data
    		fw.write(data);//appends the string to the file
   	 		fw.close();
		}
		catch(IOException ioe)
		{
    		System.err.println("IOException: " + ioe.getMessage());
		}
	}


}
