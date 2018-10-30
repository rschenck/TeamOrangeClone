import java.util.Random;

class Cell{
    static int[] maxmin={10,40};//high and low of cell cycle values
    static int deathRate = 1000;
    static Random generator = new Random(); //random number generator
    static int neoMutRate = 100;//mut 1/1000
    static int mutRate = 1000;

    int cellCycleLength;
    int positionInCellCycle;
    boolean brafRes;
    boolean PDL1;
    boolean pR;
    int[] neoAntigenLoad = new int[Fantastasize.numNA];

    public Cell(){//constructor
        this.positionInCellCycle=0;
        this.cellCycleLength=getCellCycleLength();
        this.neoAntigenLoad=getNeoAntigenLoad();
        this.brafRes = getBRAFresStat();
        this.PDL1 = getPDL1Stat();
        this.pR = getpR();
    }

    public void setInititalCondition(){
        this.PDL1=false;
        this.brafRes=false;
        this.pR=false;
        this.neoAntigenLoad[Cell.generator.nextInt(Fantastasize.numNA)]=1;
        this.cellCycleLength=maxmin[1];
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

    public boolean getBRAFresStat(){
        return this.brafRes; }

    public boolean getPDL1Stat(){
        return this.PDL1; }

    public boolean getpR(){
        return this.pR; }

    public void resetCCCycler(){
        this.positionInCellCycle=0;
    }

    public void proteomemutate(){
        if(generator.nextInt(neoMutRate)==0){//random roll - neoAntigen accumulation
            int newNA = generator.nextInt(Fantastasize.numNA);
            this.neoAntigenLoad[newNA]+=1;
            Fantastasize.totNAL[newNA]+=1;
            Fantastasize.TCR[newNA]+=1;
        }
    }

    public void phenomute(){
        if(generator.nextInt(mutRate)==0){//mutation rate
            int whichMut = generator.nextInt(3);
            if(whichMut==0){
                this.cellCycleLength=maxmin[0];
                this.pR = true;
            }
            else if(whichMut==1){
                this.brafRes=getBRAFresStat();
            }
            else{
                this.PDL1=getPDL1Stat();
            }

        }
    }
};