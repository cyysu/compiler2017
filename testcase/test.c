int N;
int M;
int i;
int j;
int primeCount;
int resultCount;
int[] b = new int[1001];
int[] prime = new int[170];
int[] gps = new int[1001];
int[] tmp = new int[1];
int[][] result;

void origin(int N)
{
	result = new int[N][];
    for (i = 0; i < N; i ++ ) {
		result[i] = new int[N];
        for (j = 0; j < N; j ++ )
        result[i][j] = 0;
    }
}

//int getPrime(int N, int* primeCount, int* b, int* gps, int* prime) {
int getPrime(int N) {
    int count;
	int i;
    count = 2;
    for (i = 2; i <= N; i = i + 1) {
        if (b[i] == 1) {
            tmp[0] = tmp[0] + 1;
            prime[tmp[0]] = i;
            gps[i] = tmp[0];
        }
        while (i * count <= N) {
            b[i * count] = 0;
            count = count + 1;
        }
        count = 2;
    }
}

//int getResult(int N, int k1, int k2, int* b, int* gps, int* prime, int result[170][170]) {
int getResult(int N, int k1, int k2) {
    if (result[k1][k2] == -1)
        if (prime[k2] * 2 - prime[k1] <= N)
            if (b[prime[k2] * 2 - prime[k1]] != 0)
                result[k1][k2] = getResult(N,
                                k2,
                                gps[prime[k2] * 2 - prime[k1]]
                                ) + 1;
    if (result[k1][k2] == -1)
        result[k1][k2] = 1;
    return result[k1][k2];
}

int printF(int k1, int k2, int k3) {
//    printf("%d", k1);
	print(toString(k1));
    while (k3 > 0) {
//        printf("%c", ' ');
		print(" ");
//        printf("%d", k2);
		print(toString(k2));
        k2 = k2 * 2 - k1;
        k1 = (k1 + k2) / 2;
        k3 = k3 - 1;
    }
//    printf("%c", '\n');
    print("\n");
}

int main() {
	origin(170);
    N = 1000;
//    M = 100*(getchar()-'0')+10*(getchar()-'0')+getchar()-'0';
	M = getInt();
    primeCount = 0;
    resultCount = 0;
    tmp[0] = 0;
    for (i = 0; i < N + 1; i = i + 1)
    {
        b[i] = 1;
        gps[i] = 0;
    }
    for (i = 0; i < M + 1; i = i + 1)
    {
        prime[i] = 0;
    }
    for (i = 0; i <= M; i = i + 1)
    {
        for (j = 0; j <= M; j = j + 1)
            result[i][j] = -1;
    }
//    getPrime(N, tmp, b, gps, prime);
    getPrime(N);
    primeCount = tmp[0];
    for (i = 1; i < primeCount; i = i + 1)
        for (j = i + 1; j <= primeCount; j = j + 1)
            if (result[i][j] == -1) {
                result[i][j] = getResult(N, i, j);
                if (result[i][j] > 1) {
                    printF(prime[i], prime[j], result[i][j]);
                    resultCount = resultCount + 1;
                }
            }
    //printf("Total: %d\n", resultCount);
	print("Total: ");
	println(toString(resultCount));
    return 0;
}
