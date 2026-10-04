# ProSci – fx-991ES Style Scientific Calculator for Android

A native Android scientific calculator crafted with Kotlin and Jetpack Compose, faithfully reproducing the **CASIO fx-991ES Natural-V.P.A.M.** experience.

## Features

- **Natural V.P.A.M. LCD Display**:
  - Exact fraction display with numerator/denominator bar.
  - Radical and $\pi$ multiples recognition.
  - S $\Leftrightarrow$ D button to toggle seamlessly between exact and decimal representations.
  - Active status flags: `S` (SHIFT), `A` (ALPHA), mode indicators, `hyp`, memory `M`, angle unit (`D`/`R`/`G`), format (`NORM`/`FIX`/`SCI`), and `Math` (MathIO).
- **Calculation Modes**:
  1. **COMP**: Standard algebraic calculations, fractions, trigonometry, powers, and logs.
  2. **CMPLX**: Complex numbers.
  3. **STAT**: Single-variable statistical summary ($n, \Sigma x, \Sigma x^2, \bar{x}, \sigma x, s_x, \min, \max, Q_1, \text{Med}, Q_3$) and 2-variable linear regression ($A + B X$, correlation coefficient $r$, $\Sigma xy$).
  4. **BASE-N**: Real-time conversions and arithmetic across Decimal, Hexadecimal, Binary, and Octal.
  5. **EQN**: Simultaneous equation solver (2 and 3 unknowns) and polynomial roots (Quadratic & Cubic with complex roots).
  6. **MATRIX**: Matrix operations up to $3\times 3$ ($A+B, A-B, A\times B, \det A, A^{-1}, A^T$).
  7. **TABLE**: Function table generator $f(X)$ evaluated from Start to End with custom Step.
  8. **VECTOR**: Vector operations in 2D and 3D ($A+B, A-B, A\cdot B, A\times B, |A|, \text{Angle}$).
  9. **CONST**: 40 CODATA physical constants ($m_p, m_e, h, \hbar, c_0, G, N_A, k, e$, etc.) with instant expression insertion.
  10. **CONV**: 40 unit conversions (Length, Area, Volume, Mass, Pressure, Energy, Temperature, Speed).
- **Calculus & Advanced Math**:
  - Numerical integration $\int$ (Simpson's rule).
  - Numerical derivative $d/dx$ (5-point central difference).
  - Discrete summation $\Sigma$.
  - Combinatorics ($nPr, nCr$) and factorials ($x!$).
- **Themes & Customization**:
  - 6 distinctive themes: Classic fx, Midnight Blue, Light Studio, Cyber Neon, Rose Gold, and High Contrast.
  - Angle unit toggling: DEG, RAD, GRA.
  - Number format cycling: NORM, FIX 2, FIX 4, SCI 3, SCI 6.
  - Calculation history with reload and clear.
  - Result clipboard copying and tactile haptic feedback.

## Tech Stack
- Kotlin 2.0.21
- Jetpack Compose with Material 3
- Android SDK 35 / Gradle 9.3.1 (Kotlin DSL)
