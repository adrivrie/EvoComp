public class EvaluateFitness{

public int evalMethod; // 0=sphere; 1=bentcigar; 2=schaffers; 3=katsuura
public int evals;
public int evalsInCurrentEvaluator;
public int max_evals;
public ContestEvaluation evaluator;
public int evaluations_limit;

    public EvaluateFitness(int evalMethod, long max_evals){
        this.evals = 0;
        this.evalMethod = evalMethod;
        this.max_evals = max_evals;
        makeEvaluator(evalMethod);
        Properties props = evaluation.getProperties();
        evaluations_limit = Integer.parseInt(props.getProperty("Evaluations"));
        
    }


    public double evaluate(double[] genotype){
        evals++;
        evalsInCurrentEvaluator++;
        if (evalsInCurrentEvaluator > evaluations_limit - 10){
            makeEvaluator(evalMethod);
        }
        if (evals > max_evals){
            print("Evaluation limit reached, you should have prevented this.")
            int a = 1/0;
        }
        return (double)evaluator.evaluate(individual.object)
    }



    private void makeEvaluator(int n){
        if (n==0){
            evaluator = new SphereEvaluation();
        } else if (n==1) {
            evaluator = new BentCigarFunction();
        } else if (n==2) {
            evaluator = new KatsuuraEvaluation();
        } else if (n==3) {
            evaluator = new SchaffersEvaluation();
        } 
    }



}