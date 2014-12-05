package irs2.src.edu.asu.irs13;

//Class to handle all matrix computations

public class MatrixComputation {
	
	public double[][] adjacencyMatrix=null;
	public int size;
	MatrixComputation(double[][] A){
//		int size = A.length;
//		for(int k=0;k<size;k++){
//			for(int j=0;j<size;j++){
//				System.out.println(A[k][j]);
//			}
		this.adjacencyMatrix = A;
		this.size = adjacencyMatrix.length;
	}

	public double[][] transpose(double[][] A){
		
		int size = A.length;
		double[][] A_trans = new double[size][size];
		System.out.println("Size"+size);
//		int size = A.length;
		for(int k=0;k<size;k++){
			for(int j=0;j<size;j++){
//				System.out.println(A[k][j]);
				A_trans[j][k] = A[k][j];
			}
		}
		return A_trans;
	}
	
	public double[][] multiply(double[][] A, double[][] B){
		int rsize = A.length;
		int csize = B[0].length;
		double[][] A_mult = new double[rsize][csize];
		for (int i = 0; i < A.length; i++) { // aRow
            for (int j = 0; j < B[0].length; j++) { // bColumn
                for (int k = 0; k < A[0].length; k++) { // aColumn
                    A_mult[i][j] += A[i][k] * B[k][j];
                }
            }
        }
		return A_mult;
	}
	
	
	public double[][] add(double[][] A, double[][] B){
		for(int i=0;i<A.length;i++)
		{
			for(int j=0;j<A.length;j++)
			{
				A[i][j] = A[i][j] + B[i][j];
			}
		}
		return A;
	}
	
	public double[][] scalarMultiply(double[][] A, double c){
		for(int i=0;i<A.length;i++){
			for(int j=0;j<A.length;j++){
				A[i][j] = A[i][j]*c;
			}
		}
		return A;
	}
}
