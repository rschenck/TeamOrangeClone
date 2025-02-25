import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;
import java.io.FileWriter;

public class Fantastasize
{
    public static int burstsize = 25; // 50, was 25
	public static int totalNA = 700; // 1333, //jill - was 700
	public static int totalImmuno = 50; // 50
	public static int TCRdelay = 4*24;
	public static int[] immunoTracker = new int[totalImmuno+totalImmuno*TCRdelay]; // Immunogenic clone population tracker
    public static int[] immunoCurrent = new int[totalImmuno]; // Current timestep sum of immunogenic cells
	public static int[] TCRpop = new int[totalImmuno];

	public static int[] TCRinactive = new int[totalImmuno];
	public static double TCRinactivationRate = 10;
	public static double reactivateFactor = 10;
	public static int totalPDL1; // How many PDL1 cells there are
	public static boolean immunoTxOn = false;
	public static int[] immunoTxStart = {30*24,50*24}; // Strating time(s) of treatment
    public static int immunoTxInterval = 7*24;
    public static int immunoATx = 4*24;
    public static double TCRDeath =0.85; // Active TCR Death Rate
    public static double TCRDeathInactive = 0.7; // Inactivated TCR Death Rate

    final static boolean jarfile = true;
    public static boolean PDL1on = true;
	public static boolean TXon = true;
	public static boolean FunctionalHet = true; // Whether Proliferation Rate varies

    static int[] ccmaxmin={20,80};//high and low of cell cycle values

    static Random generator = new Random(); //random number generator
    static int neoMutRate = 100;//mut 1/100
    static int phenoMutRate = 3;
    final static double carryingCapacity = 2000000.;
    final static int CCmodifier = 5;

    public static void main (String[] args)
	{
		//set initial condition for TCR
        for (int i=0;i<totalImmuno;i++){
            TCRpop[i]=1;
            TCRinactive[i]=0;//set inactive TCR to zero initially
        }

        String outFileName = "test.txt";
        int tmax=80*24+1; // Run Time  ------ was 365
        long maxAllowablePop=22000000;
        if(jarfile){
            outFileName = args[0];
            tmax = Integer.parseInt(args[1]);
            maxAllowablePop = Long.parseLong(args[2]);
        }

		ArrayList<Cell> cellList = new ArrayList<Cell>();
        ArrayList<Cell> AliveList = new ArrayList<Cell>();


        // Cell Initilization
        Neoantigens firstneos = new Neoantigens();
        burstInitializer(firstneos);
        cellList.add(new Cell(new int[Fantastasize.totalNA],ccmaxmin[1]));
		Cell initialCell = cellList.get(0);
		initialCell.setInititialCondition();

        int cellListSize = 0;
        int t=0;
        while (t<tmax & cellListSize<maxAllowablePop){//time loop
            t++;
            if(PDL1on && TXon) {
                immunoTxOn=(t>=immunoTxStart[1] && (t-immunoTxStart[1])% immunoTxInterval ==0)?true:immunoTxOn;
                if((t-immunoTxStart[1] - immunoATx) % immunoTxInterval ==0){
                    immunoTxOn=false;
                }
                //if(t*24 == immunoTxStart[0] | t*24 == immunoTxStart[1])
                //immunoTxOn = (t == immunoTxStart) ? true : false;
            }

            cellListSize=cellList.size();

			totalPDL1=0;
			for (int i=0; i<cellListSize; i++){//cell loop
			    Cell cell = cellList.get(i);
                if(PDL1on && TXon) {
                    totalPDL1 += (cell.PDL1) ? 1 : 0;//counts the number of PDL1 cells
                }

                //Reduce probability of division approaching carrying capacity 2*10^6
                if(cellListSize>carryingCapacity){
                    cell.advance();
                    cell.cellCycleLength=cell.cellCycleLength*CCmodifier;
                } else {
                    cell.advance();
                }

				// Births
				if (cell.isMature()){
                    cell.proteomemutate(); // Acquire neoantigens
                    cell.phenomute(); // Mutate phenotype
					Cell newC = new Cell(cell.neoAntigenLoad,cell.cellCycleLength);
					cellList.add(newC);
                    cell.resetCCCycler(); // Reset Cell Cycle Cycler
//                    System.out.println(cell.cellCycleLength+" "+newC.cellCycleLength);
				}

				// Immunogenic Clone Track This Timestep:
                int[] immunogenicity = cell.immunogenicNeos();
                for (int j = 0; j < totalImmuno; j++) {
                    immunoCurrent[j]+=immunogenicity[j];
                }

			}
            UpdateCloneTracker(t); // Update and move for previous days

            // TCR Delayed Birth Rate
            int totalSize = 0;
            for (int i = 0; i < TCRpop.length; i++) {
                TCRpop[i] += Math.pow(immunoTracker[i], 1.1);//TCR birth

                if(TCRpop[i]>1){


                    if (PDL1on) {

                        TCRinactive[i] = (int) Math.round(TCRinactive[i] * TCRDeathInactive);//death of inactive T cells

                        //TODO shift from TCRpop to TCRinactive
                        int TOTALINACTIVATED=(int) Math.round(TCRinactivationRate*totalPDL1);
                        TOTALINACTIVATED = (TOTALINACTIVATED>=TCRpop[i]) ? (int) Math.round(TCRpop[i]*.75f)-1 : TOTALINACTIVATED;

                        if(TXon){
                            if(immunoTxOn){
                                //TODO shift from TCRpop to TCRinactive
                                int TOTALREACTIVATED=(int) Math.round(reactivateFactor*TCRinactive[i]);
                                TOTALREACTIVATED = (TOTALREACTIVATED>=TCRinactive[i]) ? (int) Math.round(TCRinactive[i]) : TOTALREACTIVATED;

                                TCRinactive[i] -= TOTALREACTIVATED;
                                TCRpop[i] += TOTALREACTIVATED;//TCR death
                            } else{
                                TCRinactive[i] += TOTALINACTIVATED;
                                TCRpop[i] -= TOTALINACTIVATED;//TCR death
                            }
                        } else {
                            TCRinactive[i] += TOTALINACTIVATED;
                            TCRpop[i] -= TOTALINACTIVATED;//TCR death
                        }


                        TCRpop[i] = (int) Math.round(TCRpop[i]*TCRDeath);


                    } else {
                        TCRpop[i] = (int) Math.round(TCRpop[i]*TCRDeath);//TCR death
                        TCRpop[i] += Math.pow(immunoTracker[i], 1.1);//TCR birth
                    }
//                    if (PDL1on) {
////                        TCRinactive[i] = (int) Math.round(TCRinactive[i] * TCRDeathInactive);//death of inactive T cells
//
//                        double inactiveActiveShift=0;
//
//                        double fuck = TCRinactivationRate*totalPDL1/ (cellListSize + 0.f);
//                        double reactivate = -reactivateFactor* fuck * (TCRinactive[i]);
//                        double inactivate = fuck * (TCRpop[i] - 0*TCRinactive[i]);
////                        if(TCRpop[i] - TCRinactive[i]<0){System.out.println("ha!");}
//                        //System.out.println(reactivate+" "+inactivate);
//
//                        if (TXon) {
//                            if(immunoTxOn){
//                                inactiveActiveShift = (Math.abs(reactivate) >= TCRinactive[i] ) ? -TCRinactive[i] : reactivate;
//                            }
//                            else{
//                                inactiveActiveShift = (TCRinactive[i]>TCRpop[i]) ? TCRpop[i] : inactivate;
//                            }
//                            TCRinactive[i] += inactiveActiveShift;//shift between active and inactive cells
//                            TCRinactive[i] = (TCRinactive[i]<0) ? 0 : TCRinactive[i];
//                        } else {
//                            inactiveActiveShift = inactivate;
//                        }
//
//                    }

                } else{
                    TCRpop[i] += Math.pow(immunoTracker[i], 1.1);//TCR birth
                }
                totalSize += immunoCurrent[i] + TCRpop[i];
            }

            // Cell Death
            for (int i = 0; i < cellListSize; i++) {
                Cell cell = cellList.get(i);

                ArrayList<Integer> immunoPresence = new ArrayList<>();
                int[] cellImp = cell.immunogenicNeos();
                for (int j = 0; j < totalImmuno; j++) {
                    if (cellImp[j] == 1) {
                        immunoPresence.add(j); // Collects immunogenic epitopes
                    }
                }

                if(immunoPresence.size()>0 && t>TCRdelay){

                    double DeathProb = 0.05;
                    for (int j = 0; j < immunoPresence.size(); j++) {
                        double numerator =  (TCRpop[immunoPresence.get(j)]) * immunoCurrent[immunoPresence.get(j)];
                        double denominator = totalSize;
                        DeathProb += 0.35*numerator/denominator;
                    }
//                    System.out.println(DeathProb);

                    cellList.size();
                    if( DeathProb > generator.nextDouble() && !cell.PDL1) {

                        cell.Alive=false;

                        if(generator.nextInt(5)==0){
                            int toKill = generator.nextInt(immunoPresence.size());
                              if(TCRpop[immunoPresence.get(toKill)]>1){
                                TCRpop[immunoPresence.get(toKill)]-=1;
                            }
                        }
//                        int toKill = generator.nextInt(immunoPresence.size());
//                        if(TCRpop[immunoPresence.get(toKill)]>1){
//                            TCRpop[immunoPresence.get(toKill)]-=1;
//                        }
                    }

                }

            }

            //TODO update list with alive cells
            for (int j = 0; j < cellList.size(); j++) {
                if(cellList.get(j).Alive){
                    AliveList.add(cellList.get(j));
                }
            }
            cellList=new ArrayList<Cell>();
            cellList=AliveList;
            AliveList=new ArrayList<Cell>();
            System.gc();

            System.out.println(t + "\t" + Arrays.toString(TCRpop) + "\t" + Arrays.toString(immunoCurrent) + "\t" + cellListSize + "\t" + Arrays.toString(TCRinactive)+ "\t" +totalPDL1);
            String dataOut = t + "\t" + Arrays.toString(TCRpop) + "\t" + Arrays.toString(immunoCurrent) + "\t" + cellListSize + "\t" + Arrays.toString(TCRinactive)+ "\t" +totalPDL1 + '\n';
            write(outFileName, dataOut);

            ResetCurrentStepImmunoTracker();

		}
        System.out.println("Completed.");
	}

	public static void write (String filename, String data) {
		try{//exception handling, print a message if doesn't succeed
    		FileWriter fw = new FileWriter(filename,true); //the true will append the new data
    		fw.write(data);//appends the string to the file
   	 		fw.close();
		}
		catch(IOException ioe){
    		System.err.println("IOException: " + ioe.getMessage());
		}
	}

	public static void UpdateCloneTracker(int t){
        int[] updateTracker = new int[immunoTracker.length];
        // Step 1: Copy all but the current immunoClone slots
        for (int i = totalImmuno; i < immunoTracker.length; i++) {
            updateTracker[i-totalImmuno] = immunoTracker[i];
        }

        // Step 2: Add the Current timestep to the array in the first immunoClone slots
        for (int i = 0; i < totalImmuno; i++) {
            updateTracker[i+totalImmuno*TCRdelay] = immunoCurrent[i];
        }
        immunoTracker = updateTracker;
    }

	public static void ResetCurrentStepImmunoTracker(){
        immunoCurrent=new int[totalImmuno];
    }

	public static void burstInitializer(Neoantigens firstneos) {
        for (int i = 0; i < burstsize; i++) {
            int NAidx = Fantastasize.generator.nextInt(Fantastasize.totalNA);
            firstneos.set(NAidx);
        }
    }


}
