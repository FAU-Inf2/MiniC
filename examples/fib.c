int fib_h(int n, int a, int b) {
  if (n == 0) {
    return a;
  }
  if (n == 1) {
    return b;
  }
  return fib_h(n - 1, b, a + b);
}

int fib(int n) {
  return fib_h(n, 0, 1);
}

int main() {
  print(fib(8));
  return 0;
}
