package edu.gettysburg.redlight;

/**
 * @author Marcin Malec
 * 
 * Note: "I have added a simple fix to handle cases [in ANNPlayer] where greens_rem = 0 and red_rem is odd. This resulted in a slight improvement." 
 */
public class ANNPlayer2 extends RedLightPlayer {
	
	private static int numReds = 4, numGreens = 24, numChips = numReds + numGreens, goalScore = 50;
	private static NeuralNetwork2 ann = new NeuralNetwork2(new double[][][]{{{-0.7970040404210204, -3.2514315100752764, 22.245286759356613, 10.857268833426224, -0.8573955299957158, 12.290300336850772, -41.14597562481358, 48.799449630259446},
		{1.6854936288084195, -4.176981056119903, 25.287849024456026, 0.765748169663548, -13.477539211118437, 14.517535862737219, -10.040118421900276, 42.86680842192747},
		{6.437162728343491, -2.2640459484075968, -1.943970367163666, -3.085618197094536, 5.1668669462703685, 14.770305019124473, -11.889073994455986, 25.01246943840632},
		{-0.5834145305910885, 0.06375415753348904, -2.787085507713037, -3.1741271988282564, -7.120020287584094, 27.890159257808982, -26.771798485742213, 49.72814141299486},
		{16.846627199444303, -2.3489911983086778, 9.030994305510303, 2.398792895054531, -2.6418655936469175, -0.4370229182577226, 10.143783780318753, -13.649085204185429},
		{-13.599865054510113, 13.501308533575038, -82.71290834316686, -25.334919095413216, 36.28284086674813, 22.418534663720894, -2.2150892593079803, -21.412797191297056},
		{-76.80702209824425, -9.216077915683735, -44.06427743348619, 0.8731952447157298, -0.20328373460512333, -11.000445581960607, 7.388164479545759, 17.323626817924815},
		{-4.922403521321104, 3.3887249175151744, 1.0730119288630477, 7.512831778764013, -2.319223529029938, -83.8985592003971, 101.67235990112185, -186.07197260263828},
		{4.9356020345637885, -2.941307832558329, 10.178885014110374, 106.56649135926659, -2.7678696852401123, 60.960254912691546, -60.68120445858375, 29.069674791897597},
		{-2.023484311034844, 2.0920821674413927, -7.094676515551245, -20.044647979076423, -5.934827876432412, -8.34488558689112, 43.96791741825032, -69.01709414574775},
		{1.0057310358450533, -2.782065466351182, -22.687314311730418, -2.552423060332567, 1.006485227242237, 27.42614345274199, -32.717497655435174, 37.480437897710054},
		{-2.923050430182542, 6.5643190031618825, 0.6596426705907595, -0.03296932172857193, 1.9572601231918114, 7.024569285880918, -4.1446701150972975, 9.77268334353948},
		{-1.5751274794243137, 25.860039466528587, -3.455258632792876, 0.13152846772825783, 5.37067593566192, 2.241731730243458, 1.0904080271853058, -20.25350947860547}},
		{{-14.60070812869038, -12.923479255199181, -8.178026580604962, -9.231665283221526, -15.43234815888685, 4.770516194964778, -42.63525098649318, 17.554553447795122, -20.65795509219898, 27.468204347656435, 20.026330087740707, 17.351306355934675, 36.2258312678505, 41.46671894731301}}});

	@Override
	public void initialize(int numReds, int numGreens, int goalScore) {}

	@Override
	public int getAction(int playerScore, int opponentScore, int turnTotal,
			int redsDrawn, int greensDrawn) {
		if (playerScore + turnTotal >= goalScore ) return HOLD;
		else if (turnTotal == 0) return DRAW;
		else if(redsDrawn % 2 == 1 && greensDrawn==24) return HOLD;
		else{
			double k=(turnTotal-12.5)/11.5;
			double chipsRemaining = (numChips - redsDrawn - greensDrawn);

			double input[] = new double[]{
					(playerScore-24.5)/24.5,(opponentScore-24.5)/24.5,k,(numReds - redsDrawn-2.5)/1.5,
					(numGreens - greensDrawn-12)/12.0,(((numReds - redsDrawn)/ chipsRemaining) * k-0.52)/0.48,
					((( numGreens-greensDrawn) / chipsRemaining)-0.48)/0.48};

			return (int) Math.floor((ann.propagate(input)[0]+.5));
		}
	}

}

class NeuralNetwork2 {

	// The values of the neurons
	private double[][] neurons;

	// Weights for the network, indexed by layer number, source neuron, and destination neuron.
	private double[][][] weights;

	/**
	 * NeuralNetwork_(double[][][] weights)
	 * Creates a neural network from an array of weights.
	 * 
	 * @param weights - weights of the neural network.
	 */
	public NeuralNetwork2(double[][][] weights) {
		this.weights = weights;
		neurons = new double[weights.length][];
		//set up the hidden layers neurons
		for(int i = 0; i < neurons.length-1; ++i){
			//add one to account for the bias value
			//set the bias
			neurons[i] = new double[weights[i].length+1];
			neurons[i][weights[i].length]=1;
		}
		//set up the output layer
		int t=neurons.length-1;
		neurons[t] = new double[weights[t].length];
	}

	/** 
	 * propagate(double[] input) - Feeds the given input
	 *	 forward through the neural network and returns the 
	 *	 values for the final layer.
	 *
	 *	@param input - inputs to the neural network
	 */
	public double[] propagate(double... input) {
		//modify the input to have bias value of 1
		double in[]= new double[input.length+1];
		int i=0;
		for(; i < input.length; ++i){
			in[i]=input[i];
		}
		in[i]=1;
		//propagate the values from input layer to the next layers
		for(int j=0; j < weights.length; ++j){
			//for each neuron in the layer
			for(int k=0; k < weights[j].length; ++k){
				double sigma=0;
				//for each weight that is connected to the neuron
				for(int l=0; l<weights[j][k].length ; ++l){
					sigma += weights[j][k][l] * (j<1? in[l]:neurons[j-1][l]);
				}
				//store the sigma
				neurons[j][k]=sigma;
				//store the activated value
				neurons[j][k]= (1.0 / (1.0 + Math.exp(-sigma)));
			}
		}
		return neurons[neurons.length-1];
	}
}