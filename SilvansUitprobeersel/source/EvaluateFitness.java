import java.util.Properties;

import org.vu.contest.ContestEvaluation;


public class EvaluateFitness{
	final static int FUNCTION_SPHERE = 0;
	final static int FUNCTION_BENT_CIGAR = 1;
	final static int FUNCTION_SCHAFFERS = 2;
	final static int FUNCTION_KATSUURA = 3;

public int evalMethod; // 0=sphere; 1=bentcigar; 2=schaffers; 3=katsuura
public int evals;
public int evalsInCurrentEvaluator;
public long max_evals;
public ContestEvaluation evaluator;
public int evaluations_limit;

    public EvaluateFitness(int evalMethod, long max_evals){
        this.evals = 0;
        this.evalMethod = evalMethod;
        this.max_evals = max_evals;
        makeEvaluator(evalMethod);
        Properties props = evaluator.getProperties();
        evaluations_limit = Integer.parseInt(props.getProperty("Evaluations"));
        
    }


    public double evaluate(double[] genotype){
        evals++;
        evalsInCurrentEvaluator++;
        if (evalsInCurrentEvaluator > evaluations_limit - 10){
            makeEvaluator(evalMethod);
            evalsInCurrentEvaluator = 0;
        }
        if (evals > max_evals){
            System.out.println("Evaluation limit reached, you should have prevented this.");
            int a = 1/0;
        }
        return (double)evaluator.evaluate(genotype);
    }



    private void makeEvaluator(int n){
        if (n==FUNCTION_SPHERE){
            evaluator = new SphereEvaluation();
        } else if (n==FUNCTION_BENT_CIGAR) {
            evaluator = new BentCigarFunction();
        } else if (n==FUNCTION_KATSUURA) {
            evaluator = new KatsuuraEvaluation();
        } else if (n==FUNCTION_SCHAFFERS) {
            evaluator = new SchaffersEvaluation();
        } 
    }



}