package newClass;

public class Cost_tolerance {
	public static double  Ctolerance(double costC,double costS,double per)
	{
		//the cost percentage is per,C is the cost of computation;S is the cost of store
		double C=costC;
		double S=costS;
		double a=0.0;
		double b=0.0;
		double c=0.0;
		double Per=per;
		a=S;
		b=-(2*Per+1)*(C+S);
		c=C;
		double x1;
		double x2;
		x1=(-b+Math.sqrt(b*b-4*a*c))/(2*a);
		x2=(-b-Math.sqrt(b*b-4*a*c))/(2*a);
		if(x1<x2)
			return x1;
		else 
			return x2;
	}
//	public static double Ctolerance_n(double costC, double costS, double n)
//	{
//		//愿意多花的钱为n
//		double C=costC;
//		double S=costS;
//		double a=0.0;
//		double b;
//		double c;
//		double N=n;
//		a=S;
//		b=-(C+S+2*N);
//		c=C;
//		double x1;
//		double x2;
//		x1=(-b+Math.sqrt(b*b-4*a*c))/(2*a);
//		x2=(-b-Math.sqrt(b*b-4*a*c))/(2*a);
//		if(x1<x2)
//			return x1;
//		else 
//			return x2;
//	}
//      
}
