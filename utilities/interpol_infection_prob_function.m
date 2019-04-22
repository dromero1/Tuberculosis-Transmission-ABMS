% Known points
a = 0;
b = 61320;
xm = -61320;

% SLE
M = [a^2 a 1; b^2 b 1; xm^2 xm 1];
N = [0.10 0 0]';
X = M\N;

% Get polynomial coefficients
A = X(1);
B = X(2);
C = X(3);

% Build interpolating polynomial
syms x;
p = A*x^2 + B*x + C;

% Plot polynomial
ezplot(p, a, b);