int foo() {
  print(13);
  return 1;
}

int main() {
  if (1 == 1 || foo() == 1) {
    print(3);
  }
  return 0;
}
