void collatz(int n) {
  print(n);

  while (n != 1) {
    if (n / 2 != (n + 1) / 2) {
      n = 3 * n + 1;
    } else {
      n = n / 2;
    }
    print(n);
  }
}

int main() {
  int n;
  n = 19;

  collatz(n);

  return 0;
}
