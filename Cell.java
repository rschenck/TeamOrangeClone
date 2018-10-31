import java.util.Random;

class Cell{
    static int[] maxmin={10,40};//high and low of cell cycle values
    static int deathRate = 1000;

    int cellCycleLength;
    int positionInCellCycle;
    boolean brafRes;
    boolean PDL1;
    boolean pR;
    public Neoantigens neoAntigenLoad;


    public Cell(Neoantigens neos){//constructor
        this.positionInCellCycle=0;
        this.cellCycleLength=getCellCycleLength();
        this.neoAntigenLoad=this.inheritNeos(neos);
        this.brafRes = getBRAFresStat();
        this.PDL1 = getPDL1Stat();
        this.pR = getpR();
    }

    public void setInititialCondition(){
        this.PDL1=false;
        this.brafRes=false;
        this.pR=false;
//        this.neoAntigenLoad[Cell.generator.nextInt(Fantastasize.numNA)]=1;
        this.cellCycleLength=maxmin[1];
    }

    public Neoantigens inheritNeos(Neoantigens neos){
        Neoantigens newNeos = new Neoantigens();
        for (int i = 0; i < neos.getNeoLoadLength(); i++) {
            if( neos.get(i)==1){
                newNeos.set(i);
            }
        }
        return(newNeos);
    }

    public void advance() {
        positionInCellCycle++;}

    public boolean isMature(){
        return positionInCellCycle>=this.cellCycleLength; }

    public int getPositionInCellCycle(){
        return this.positionInCellCycle; }

    public int getCellCycleLength(){
        return this.cellCycleLength; }

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
        if(Fantastasize.generator.nextInt(Fantastasize.neoMutRate)==0){//random roll - neoAntigen accumulation
            int NAidx = Fantastasize.generator.nextInt(Fantastasize.totalNA);
            this.neoAntigenLoad.set(NAidx);
//            Fantastasize.totNAL[newNA]+=1;
//            Fantastasize.TCRpop[newNA]+=1;
        }
    }

    public void phenomute(){
        if(Fantastasize.generator.nextInt(Fantastasize.mutRate)==0){//mutation rate
            int whichMut = Fantastasize.generator.nextInt(3);
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