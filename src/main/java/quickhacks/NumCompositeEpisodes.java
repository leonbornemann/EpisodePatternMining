package quickhacks;

public class NumCompositeEpisodes {

	public static void main(String[] args) {
		int length = 10;
		System.out.println(numCompositeEpisodes(length));

	}

	private static long numCompositeEpisodes(int n) {
		long result = 0;
		for(int k=1;k<n;k++){
			long tempResult = 1;
			for(int i=0;i<=k;i++){
				tempResult = tempResult*(n-i);
			}
			result +=tempResult;
		}
		return result;
	}

}
