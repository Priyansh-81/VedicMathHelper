package com.priyansh.vedicMaths;

import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SutraActivity extends AppCompatActivity {

    TextView sutraTitle, questionText, hintText, solutionText, progressText;
    LinearLayout questionContainer;
    EditText answerInput;
    Button checkBtn, nextBtn;
    ImageButton infoBtn, demoBtn;
    ProgressBar levelProgressBar;
    KonfettiView konfettiView;

    int sutraNumber;
    int a, b, c, d, correctAnswer;
    int currentStep = 0;
    int maxSteps = 1;
    int intermediateTarget = 0;

    boolean isDemo = true;
    Random random = new Random();

    private View tutorialOverlay;
    private int tutorialStep = 0;
    private int problemsSolved = 0;
    private static final int DEMO_PROBLEMS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sutra);

        sutraNumber = getIntent().getIntExtra("sutraNumber", 1);

        sutraTitle = findViewById(R.id.sutraTitle);
        questionText = findViewById(R.id.questionText);
        questionContainer = findViewById(R.id.questionContainer);
        hintText = findViewById(R.id.hintText);
        solutionText = findViewById(R.id.solutionText);
        progressText = findViewById(R.id.progressText);
        answerInput = findViewById(R.id.answerInput);
        checkBtn = findViewById(R.id.checkBtn);
        nextBtn = findViewById(R.id.nextBtn);
        infoBtn = findViewById(R.id.infoBtn);
        demoBtn = findViewById(R.id.demoBtn);
        levelProgressBar = findViewById(R.id.levelProgressBar);
        konfettiView = findViewById(R.id.konfettiView);

        sutraTitle.setText(getSutraName(sutraNumber));

        if (sutraNumber == 4 || sutraNumber == 7 || sutraNumber == 9 || sutraNumber == 13) {
            answerInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        }

        loadInitialProgress();
        startSutraFlow();
        checkFirstTimeTutorial();

        checkBtn.setOnClickListener(v -> {
            performHaptic(v);
            checkAnswer();
        });

        infoBtn.setOnClickListener(v -> {
            performHaptic(v);
            showInstructionsPopup();
        });

        demoBtn.setOnClickListener(v -> {
            performHaptic(v);
            showDemoPopup();
        });

        nextBtn.setOnClickListener(v -> {
            performHaptic(v);
            problemsSolved++;
            if (problemsSolved < DEMO_PROBLEMS) {
                isDemo = true;
            } else {
                isDemo = false;
            }
            
            generateQuestion();
            currentStep = 0;
            
            if (isDemo) {
                startDemo();
            } else {
                startPractice();
            }
        });
    }

    private void checkFirstTimeTutorial() {
        android.content.SharedPreferences prefs = getSharedPreferences("VedicPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("first_time_sutra", true)) {
            showTutorial();
            prefs.edit().putBoolean("first_time_sutra", false).apply();
        }
    }

    private void showTutorial() {
        tutorialOverlay = getLayoutInflater().inflate(R.layout.overlay_tutorial, null);
        addContentView(tutorialOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        TextView title = tutorialOverlay.findViewById(R.id.tutorialTitle);
        TextView message = tutorialOverlay.findViewById(R.id.tutorialMessage);
        Button next = tutorialOverlay.findViewById(R.id.tutorialNextBtn);
        
        final int[] tutorialTitles = {
            R.string.tutorial_welcome_title,
            R.string.tutorial_stepwise_title,
            R.string.tutorial_demo_title,
            R.string.tutorial_ready_title
        };
        
        final int[] tutorialMsgs = {
            R.string.tutorial_welcome_msg,
            R.string.tutorial_stepwise_msg,
            R.string.tutorial_demo_msg,
            R.string.tutorial_ready_msg
        };

        next.setOnClickListener(v -> {
            tutorialStep++;
            if (tutorialStep < tutorialTitles.length) {
                title.setText(tutorialTitles[tutorialStep]);
                message.setText(tutorialMsgs[tutorialStep]);
                if (tutorialStep == tutorialTitles.length - 1) {
                    next.setText(R.string.tutorial_btn_start);
                }
            } else {
                ((android.view.ViewGroup) tutorialOverlay.getParent()).removeView(tutorialOverlay);
            }
        });
    }

    private void startSutraFlow() {
        problemsSolved = 0;
        isDemo = true;
        generateQuestion();
        startDemo();
    }

    private void showInstructionsPopup() {
        showCustomDialog(getSutraName(sutraNumber), getSutraLongDescription(sutraNumber), getString(R.string.dialog_got_it));
    }

    private void showDemoPopup() {
        showCustomDialog(getString(R.string.quick_demo_prefix, getSutraName(sutraNumber)), getDemoContent(), getString(R.string.dialog_lets_practice));
    }

    private void showCustomDialog(String title, String message, String buttonText) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_sutra, null);
        
        TextView titleTv = dialogView.findViewById(R.id.dialogTitle);
        TextView messageTv = dialogView.findViewById(R.id.dialogMessage);
        Button actionBtn = dialogView.findViewById(R.id.dialogButton);
        
        titleTv.setText(title);
        messageTv.setText(message);
        actionBtn.setText(buttonText);
        
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        actionBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String getSutraLongDescription(int id) {
        switch (id) {
            case 1: return "Ekadhikena Purvena (By one more than the previous one)\n\n" +
                    "Applicable to: Squaring numbers ending in 5.\n\n" +
                    "Method:\n" +
                    "1. Take the digits before the 5.\n" +
                    "2. Multiply this number by its successor (n × (n+1)).\n" +
                    "3. Append '25' to the result.\n\n" +
                    "Example: 35²\n" +
                    "3 × (3+1) = 12\n" +
                    "Result: 1225";
            case 2: return "Nikhilam Navatashcaramam Dashatah (All from 9 and the last from 10)\n\n" +
                    "Applicable to: Multiplication of numbers near base 10, 100, 1000 etc.\n\n" +
                    "Method:\n" +
                    "1. Find the deficiency of both numbers from the base.\n" +
                    "2. Right part of answer is product of deficiencies.\n" +
                    "3. Left part is cross-subtraction of one number and other's deficiency.\n\n" +
                    "Example: 98 × 97 (Base 100)\n" +
                    "Deficiencies: 02, 03\n" +
                    "Left: 98-3 = 95 | Right: 2×3 = 06\n" +
                    "Result: 9506";
            case 3: return "Urdhva-Tiryagbhyam (Vertically and Crosswise)\n\n" +
                    "Applicable to: General multiplication of any numbers.\n\n" +
                    "Method for 2-digit numbers (ab × cd):\n" +
                    "1. Units vertical: b × d\n" +
                    "2. Crosswise: (a×d + b×c)\n" +
                    "3. Tens vertical: a × c\n" +
                    "4. Combine parts using the Carry Rule (keep only one digit in each part except the leftmost).";
            case 4: return "Paravartya Yojayet (Transpose and Apply)\n\n" +
                    "Applicable to: Division when the divisor is slightly above a power of 10.\n\n" +
                    "Method:\n" +
                    "1. Identify the base (10, 100 etc.) and the surplus.\n" +
                    "2. Transpose the surplus digits (change their sign).\n" +
                    "3. Perform the division steps using these transposed digits by multiplying and adding horizontally.";
            case 5: return "Shunyam Saamyasamuccaye (When the Samuccaye is the same, that Samuccaye is zero)\n\n" +
                    "Applicable to: Equations of various forms, e.g., (x+a)(x+b) = (x+c)(x+d) where ab=cd.\n\n" +
                    "Method:\n" +
                    "If the product of constants is equal on both sides, then x = 0.";
            case 6: return "Anurupyena (By Proportion)\n\n" +
                    "Applicable to: Squaring numbers near a sub-base (like 20, 30, 40 etc.).\n\n" +
                    "Method:\n" +
                    "1. Find the deviation from the sub-base.\n" +
                    "2. Add the deviation to the number and multiply by the sub-base ratio.\n" +
                    "3. Square the deviation and append (with carry rule).\n\n" +
                    "Example: 22² (Sub-base 20, ratio 2)\n" +
                    "1. Deviation: +2\n" +
                    "2. Left: (22+2) × 2 = 48\n" +
                    "3. Right: 2² = 4\n" +
                    "Result: 484";
            case 7: return "Sankalana-Vyavakalanabhyam (By Addition and By Subtraction)\n\n" +
                    "Applicable to: Solving simultaneous equations where coefficients are interchanged.\n\n" +
                    "Method:\n" +
                    "1. Add the two equations to get a simplified (x+y) form.\n" +
                    "2. Subtract the two equations to get a simplified (x-y) form.\n" +
                    "3. Solve the resulting simple equations for x and y.";
            case 8: return "Puranapuranabhyam (By Completion or Non-completion)\n\n" +
                    "Applicable to: Addition and subtraction by completing the next multiple of 10.\n\n" +
                    "Example: 48 + 7\n" +
                    "Take 2 from 7 to make 48 into 50. Then 50 + 5 = 55.";
            case 9: return "Chalana-Kalanabhyam (Sequential Motion)\n\n" +
                    "Applicable to: Factorizing quadratic equations of the form x² + (a+b)x + ab = 0.\n\n" +
                    "Method:\n" +
                    "1. Find two numbers that sum to the middle coefficient (a+b) and multiply to the constant term (ab).\n" +
                    "2. The factors are (x+a) and (x+b).\n" +
                    "3. The roots are x = -a and x = -b.";
            case 10: return "Yavadunam (By the Deficiency)\n\n" +
                    "Applicable to: Squaring numbers near a base (like 100).\n\n" +
                    "Method:\n" +
                    "1. Find the deficiency 'd' from the base.\n" +
                    "2. Subtract 'd' from the number: (n - d).\n" +
                    "3. Square the deficiency: d².\n" +
                    "4. Append (n-d) and d².";
            case 11: return "Vyashtisamanstih (Individuality and Totality)\n\n" +
                    "Applicable to: Verification of factorization and solving complex equations.\n\n" +
                    "Method for Scrutiny:\n" +
                    "The Sum of Coefficients (Sc) of the product is equal to the product of the Sc of its factors.\n\n" +
                    "Example: (x+2)(x+3) = x² + 5x + 6\n" +
                    "Sc of (x+2) = 1+2 = 3\n" +
                    "Sc of (x+3) = 1+3 = 4\n" +
                    "Product of Sc = 3 × 4 = 12\n\n" +
                    "Sc of (x² + 5x + 6) = 1 + 5 + 6 = 12.\n" +
                    "Since 12 = 12, the factorization is correct!";
            case 12: return "Shesanyankena Charamena (The remainders by the last digit)\n\n" +
                    "Applicable to: Division of numbers ending in 9 (or similar).\n\n" +
                    "Method:\n" +
                    "1. For a divisor ending in 9 (like 19), use 2 (one more than the first digit) as the auxiliary divisor.\n" +
                    "2. Divide the digits from left to right, carrying remainders to the next step.\n\n" +
                    "Example: 1/19\n" +
                    "Divide 1 by 2: Q=0, R=1 -> Next digit is 10\n" +
                    "Divide 10 by 2: Q=5, R=0 -> Next digit is 5\n" +
                    "Divide 5 by 2: Q=2, R=1 -> Next digit is 12...";
            case 13: return "Sopantyadvayamantyam (The ultimate and twice the penultimate)\n\n" +
                    "Applicable to: Equations like 1/(x+a) + 1/(x+b) = 0.\n\n" +
                    "Method:\n" +
                    "According to this Sutra, for equations of this form, the sum of the denominators is zero.\n" +
                    "(x+a) + (x+b) = 0\n" +
                    "2x + (a+b) = 0\n" +
                    "x = -(a+b)/2\n\n" +
                    "Example: 1/(x+2) + 1/(x+4) = 0\n" +
                    "2x + 6 = 0  => x = -3";
            case 14: return "Ekanyunena Purvena (By one less than the previous one)\n\n" +
                    "Applicable to: Multiplication of any number by 9, 99, 999, etc.\n\n" +
                    "Method:\n" +
                    "1. Left Part: Subtract 1 from the multiplicand (number being multiplied).\n" +
                    "2. Right Part: Find the 9s complement of the Left Part (subtract each digit from 9).\n\n" +
                    "Example: 43 × 99\n" +
                    "1. Left Part: 43 - 1 = 42\n" +
                    "2. Right Part: 99 - 42 = 57\n" +
                    "Result: 4257";
            case 15: return "Gunakasamuccayah (The product of the sum of the coefficients...)\n\n" +
                    "Applicable to: Verifying results using Digit Sums (Casting out 9s).\n\n" +
                    "Rule: The digit sum of the product equals the product of the digit sums of the factors.";
            case 16: return "Gunita Samuccayah (The product of the sum...)\n\n" +
                    "Applicable to: Verifying polynomial factorization.\n\n" +
                    "Rule: In a quadratic expression (x+a)(x+b) = x² + (a+b)x + ab:\n" +
                    "Sum of coefficients (Sc) of the product = product of the Sc of each factor.\n\n" +
                    "Example: (x+1)(x+2) = x² + 3x + 2\n" +
                    "Sc(f1) = 1+1=2\n" +
                    "Sc(f2) = 1+2=3\n" +
                    "Sc(Product) = 2×3 = 6\n" +
                    "Verify: 1+3+2 = 6. Matches!";
            default: return "This Vedic Sutra provides a high-speed mental calculation technique. Follow the step-by-step instructions in the practice mode to master its application!";
        }
    }

    private void startPractice() {
        answerInput.setVisibility(View.VISIBLE);
        checkBtn.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.GONE);
        solutionText.setText(getString(R.string.start_practice));
        solutionText.setTextColor(getResources().getColor(android.R.color.black));
        loadStep();
    }

    private void performHaptic(View view) {
        if (AppSettings.isHapticsEnabled(this)) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
        }
    }

    private String getSutraName(int id) {
        switch (id) {
            case 1: return "Ekadhikena Purvena";
            case 2: return "Nikhilam Navatashcaramam Dashatah";
            case 3: return "Urdhva-Tiryagbhyam";
            case 4: return "Paravartya Yojayet";
            case 5: return "Shunyam Saamyasamuccaye";
            case 6: return "Anurupyena";
            case 7: return "Sankalana-Vyavakalanabhyam";
            case 8: return "Puranapuranabhyam";
            case 9: return "Chalana-Kalanabhyam";
            case 10: return "Yavadunam";
            case 11: return "Vyashtisamanstih";
            case 12: return "Shesanyankena Charamena";
            case 13: return "Sopantyadvayamantyam";
            case 14: return "Ekanyunena Purvena";
            case 15: return "Gunakasamuccayah";
            case 16: return "Gunita Samuccayah";
            default: return "Sutra " + id;
        }
    }

    private void generateQuestion() {
        answerInput.setText("");
        solutionText.setText("");
        
        switch (sutraNumber) {
            case 1: // Ekadhikena (square ending 5)
                int tens1 = random.nextInt(9) + 1;
                a = tens1 * 10 + 5;
                b = a;
                maxSteps = isDemo ? 2 : 1;
                correctAnswer = a * b;
                break;
            case 2: // Nikhilam (near 100)
                a = 100 - (random.nextInt(12) + 1);
                b = 100 - (random.nextInt(12) + 1);
                maxSteps = isDemo ? 3 : 1;
                correctAnswer = a * b;
                break;
            case 3: // Urdhva (2x2 or 3x2)
                if (random.nextBoolean()) {
                    // 2x2
                    a = random.nextInt(89) + 11;
                    b = random.nextInt(89) + 11;
                    maxSteps = isDemo ? 4 : 1;
                } else {
                    // 3x2
                    a = random.nextInt(899) + 101;
                    b = random.nextInt(89) + 11;
                    maxSteps = isDemo ? 5 : 1;
                }
                correctAnswer = a * b;
                break;
            case 4: // Paravartya (Division by divisor slightly above base)
                // divisor will be 1x (11, 12, 13, 14)
                b = 10 + random.nextInt(4) + 1;
                // Pick integer quotient to ensure exact division for clean learning
                int quotient4 = random.nextInt(30) + 10;
                a = b * quotient4;
                maxSteps = isDemo ? 5 : 1;
                correctAnswer = quotient4;
                break;
            case 5: // Shunyam (Specific case: (x+a)(x+b) = (x+c)(x+d) where ab=cd)
                a = random.nextInt(8) + 2;
                b = random.nextInt(8) + 2;
                c = a * b;
                d = 1;
                maxSteps = isDemo ? 2 : 1;
                correctAnswer = 0;
                break;
            case 6: // Anurupyena (Squaring near sub-base)
                int subBase = (random.nextInt(4) + 2) * 10;
                a = subBase + (random.nextInt(5) - 2);
                b = a;
                maxSteps = isDemo ? 2 : 1;
                correctAnswer = a * b;
                break;
            case 7: // Sankalana (Simultaneous equations)
                a = random.nextInt(20) + 10;
                b = random.nextInt(20) + 10;
                if (a == b) b++;
                
                // Pick integer roots first to ensure integer solutions
                int xVal = random.nextInt(9) + 1;
                int yVal = random.nextInt(9) + 1;
                
                c = a * xVal + b * yVal;
                d = b * xVal + a * yVal;
                
                maxSteps = isDemo ? 3 : 1;
                correctAnswer = xVal; // target x
                break;
            case 8: // Puranapuranabhyam (Completion of 10s)
                a = (random.nextInt(8) + 1) * 10 + (random.nextInt(4) + 5);
                b = random.nextInt(5) + 6;
                maxSteps = isDemo ? 2 : 1;
                correctAnswer = a + b;
                break;
            case 9: // Chalana-Kalanabhyam (Used for quadratics: x² + (a+b)x + ab)
                a = random.nextInt(5) + 1; // root 1
                b = random.nextInt(5) + 1; // root 2
                // (x+a)(x+b) = x² + (a+b)x + ab
                // Let's ask to solve for x when x² + (a+b)x + ab = 0
                maxSteps = isDemo ? 2 : 1;
                correctAnswer = -a; // Just one of the roots for simplicity
                break;
            case 10: // Yavadunam (Squaring near base)
                int base = 100;
                a = base - (random.nextInt(15) + 1);
                b = a;
                maxSteps = isDemo ? 3 : 1;
                correctAnswer = a * b;
                break;
            case 11: // Vyashtisamanstih (Factorization)
                a = random.nextInt(5) + 1;
                b = random.nextInt(5) + 6;
                // Question: x² + (a+b)x + ab
                maxSteps = isDemo ? 3 : 1;
                correctAnswer = (1 + a) * (1 + b); // Target is Sc of total
                break;
            case 12: // Shesanyankena Charamena (Special Division)
                // Divisor ends in 9, e.g., 19, 29
                b = (random.nextInt(3) + 1) * 10 + 9;
                // Simple case: finding the first few decimal digits of 1/b
                a = 1; 
                maxSteps = isDemo ? 3 : 1;
                // For simplicity in this app, we'll use it to find the first decimal digit
                correctAnswer = (10 / ((b + 1) / 10)); 
                break;
            case 13: // Sopantyadvayamantyam
                a = random.nextInt(10) + 1;
                b = random.nextInt(10) + 11;
                // Ensure a + b is even for integer result
                if ((a + b) % 2 != 0) b++;
                maxSteps = isDemo ? 2 : 1;
                correctAnswer = -(a + b) / 2;
                break;
            case 14: // Ekanyunena (x99)
                a = random.nextInt(89) + 10;
                b = 99;
                maxSteps = isDemo ? 3 : 1;
                correctAnswer = a * b;
                break;
            case 15: // Gunakasamuccayah (Verification)
                a = random.nextInt(10) + 11;
                b = random.nextInt(10) + 11;
                maxSteps = isDemo ? 2 : 1;
                correctAnswer = a * b;
                break;
            case 16: // Gunita Samuccayah (Verification of polynomial factors)
                a = random.nextInt(4) + 1; // factor 1: (x + a)
                b = random.nextInt(4) + 1; // factor 2: (x + b)
                // Question: Verify (x+a)(x+b) = x² + (a+b)x + ab
                maxSteps = isDemo ? 3 : 1;
                correctAnswer = (1 + a) * (1 + b); // Target is Sc of the expression
                break;
            default:
                a = random.nextInt(20) + 5;
                b = random.nextInt(20) + 5;
                maxSteps = isDemo ? 2 : 1;
                correctAnswer = a * b;
        }
    }


    private void startDemo() {
        answerInput.setVisibility(View.VISIBLE);
        checkBtn.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.GONE);
        
        // Brief explanation of the technique for the first demo problem
        if (problemsSolved == 0) {
            playDemo();
            showDemoPopup();
        } else {
            solutionText.setText("Demo Problem " + (problemsSolved + 1) + " of " + DEMO_PROBLEMS);
            solutionText.setTextColor(getResources().getColor(android.R.color.black));
        }
        loadStep();
    }

    private String getDemoContent() {
        StringBuilder demo = new StringBuilder();
        
        switch (sutraNumber) {
            case 1:
                demo.append("Sutra: Ekadhikena Purvena\n\n");
                demo.append("Problem: ").append(a).append("²\n\n");
                demo.append("1. Take the digit before 5: ").append(a/10).append("\n");
                demo.append("2. Multiply by its successor (").append(a/10 + 1).append("):\n");
                demo.append("   ").append(a/10).append(" × ").append(a/10 + 1).append(" = ").append((a/10) * (a/10 + 1)).append("\n");
                demo.append("3. Append '25' to the result.\n\n");
                demo.append("Final Answer: ").append(correctAnswer);
                break;
            case 3:
                int a1 = a/10, a0 = a%10;
                int b1 = b/10, b0 = b%10;
                demo.append("Sutra: Urdhva-Tiryagbhyam\n\n");
                demo.append("Problem: ").append(a).append(" × ").append(b).append("\n\n");
                demo.append("• Vertical Units: ").append(a0).append("×").append(b0).append(" = ").append(a0*b0).append("\n");
                demo.append("• Crosswise Sum: (").append(a1).append("×").append(b0).append(") + (").append(a0).append("×").append(b1).append(") = ").append(a1*b0 + a0*b1).append("\n");
                demo.append("• Vertical Tens: ").append(a1).append("×").append(b1).append(" = ").append(a1*b1).append("\n");
                demo.append("\n• Combine using carry rule.\n\n");
                demo.append("Final Answer: ").append(correctAnswer);
                break;
            default:
                demo.append("Problem: ").append(a).append(a == b ? "²" : " × " + b).append("\n\n");
                demo.append("Step-by-step logic:\n");
                demo.append("The app will guide you through ").append(maxSteps).append(" interactive steps to solve this using the Vedic method.");
        }
        return demo.toString();
    }

    private void playDemo() {
        solutionText.setAlpha(0f);
        solutionText.animate().alpha(1f).setDuration(600).start();
        String demoText = "Demo Problem " + (problemsSolved + 1) + "/" + DEMO_PROBLEMS + "\n\n";

        switch (sutraNumber) {
            case 1:
                demoText += "Multiply the first digit by 'one more than itself' and append 25.\n\nExample: " + a + "²\n" + (a / 10) + " × " + (a / 10 + 1) + " = " + ((a / 10) * (a / 10 + 1)) + "\nResult: " + (a * a);
                break;
            case 2:
                int defA = 100 - a;
                int defB = 100 - b;
                demoText += "Find deficiency from 100.\n" + a + " × " + b + "\nDeficiencies: " + defA + ", " + defB + "\nResult: " + (a * b);
                break;
            case 3:
                if (a < 100) {
                    int a1 = a / 10, a0 = a % 10;
                    int b1 = b / 10, b0 = b % 10;
                    demoText += "Urdhva-Tiryagbhyam (Vertically and Crosswise)\n" +
                            "1. Units Vertical: " + a0 + "×" + b0 + " = " + (a0 * b0) + "\n" +
                            "2. Crosswise: (" + a1 + "×" + b0 + ") + (" + a0 + "×" + b1 + ") = " + (a1 * b0 + a0 * b1) + "\n" +
                            "3. Tens Vertical: " + a1 + "×" + b1 + " = " + (a1 * b1) + "\n" +
                            "Combine with carries to get " + (a * b);
                } else {
                    int a2 = a / 100, a1 = (a / 10) % 10, a0 = a % 10;
                    int b1 = b / 10, b0 = b % 10;
                    demoText += "Urdhva-Tiryagbhyam (3x2 Digit)\n" +
                            "1. Units: " + a0 + "×" + b0 + " = " + (a0 * b0) + "\n" +
                            "2. 1st Cross: (" + a1 + "×" + b0 + " + " + a0 + "×" + b1 + ") = " + (a1 * b0 + a0 * b1) + "\n" +
                            "3. 2nd Cross: (" + a2 + "×" + b0 + " + " + a1 + "×" + b1 + ") = " + (a2 * b0 + a1 * b1) + "\n" +
                            "4. Leftmost: " + a2 + "×" + b1 + " = " + (a2 * b1) + "\n" +
                            "Result: " + (a * b);
                }
                break;
            case 7:
                demoText += "Sankalana-Vyavakalanabhyam (Add and Subtract)\n" +
                        "Add eqs: (" + a + "+" + b + ")x + (" + b + "+" + a + ")y = " + (c + d) + "\n" +
                        "Divide to get x + y = " + ((c + d) / (a + b)) + "\n" +
                        "Repeat by subtracting to find x - y, then solve.";
                break;
            case 4:
                int div4 = b;
                int mod4 = b % 10;
                demoText += "Paravartya Yojayet (Transpose and Apply)\n" +
                        "Used for division when divisor is slightly above a base.\n" +
                        "1. Divisor: " + div4 + " (Base 10)\n" +
                        "2. Transpose digit: " + mod4 + " becomes -" + mod4 + "\n" +
                        "3. Multiply and Add diagonally to find Quotient and Remainder.\n" +
                        "Example: " + a + " ÷ " + b;
                break;
            case 14:
                demoText += "Ekanyunena Purvena (Multiplication by 99)\n" +
                        "1. Left Part: " + a + " - 1 = " + (a - 1) + "\n" +
                        "2. Right Part: 99 - " + (a - 1) + " = " + (99 - (a - 1)) + "\n" +
                        "Combine: " + (a - 1) + " | " + (99 - (a - 1)) + " = " + (a * b);
                break;
            default:
                demoText += "Vedic Method for Sutra " + sutraNumber + "\nProblem: " + a + (a == b ? "²" : " × " + b) + " = " + correctAnswer;
        }
        solutionText.setText(demoText);
    }

    private void loadStep() {
        answerInput.setText("");
        questionContainer.removeAllViews();
        int textColor = getResources().getColor(R.color.black);
        
        // Handle Question Layout
        if (sutraNumber == 3) {
            questionText.setVisibility(View.GONE);
            
            TextView tv = new TextView(this);
            tv.setTextSize(52);
            tv.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
            tv.setTextColor(textColor);
            tv.setGravity(android.view.Gravity.END);
            
            // Format numbers to fixed 3-char width for perfect alignment
            // Use non-breaking spaces for padding to prevent Android from collapsing them
            String sA = String.format(Locale.getDefault(), "%3d", a);
            String sB = String.format(Locale.getDefault(), "%3d", b);
            
            String line1 = "\u00A0\u00A0" + sA; // Pad for alignment with symbol
            String line2 = "× " + sB;
            String line3 = "—————";
            
            tv.setText(line1 + "\n" + line2 + "\n" + line3);
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
            questionContainer.addView(tv, lp);
        } else if (sutraNumber == 7) {
            questionText.setVisibility(View.GONE);
            TextView eq1 = new TextView(this);
            eq1.setText(String.format(Locale.getDefault(), "%dx + %dy = %d", a, b, c));
            eq1.setTextSize(26);
            eq1.setTextColor(textColor);
            
            TextView eq2 = new TextView(this);
            eq2.setText(String.format(Locale.getDefault(), "%dx + %dy = %d", b, a, d));
            eq2.setTextSize(26);
            eq2.setTextColor(textColor);
            
            questionContainer.addView(eq1);
            questionContainer.addView(eq2);
        } else if (sutraNumber == 5) {
            questionText.setVisibility(View.VISIBLE);
            questionText.setText(String.format(Locale.getDefault(), "(x+%d)(x+%d) = (x+%d)(x+%d)", a, b, c, d));
            questionText.setTextSize(22);
            questionText.setTextColor(textColor);
            questionContainer.addView(questionText);
        } else if (sutraNumber == 4) {
            questionText.setVisibility(View.VISIBLE);
            questionText.setText(a + " ÷ " + b);
            questionText.setTextSize(40);
            questionText.setTextColor(textColor);
            questionContainer.addView(questionText);
        } else if (sutraNumber == 8) {
            questionText.setVisibility(View.VISIBLE);
            questionText.setText(a + " + " + b);
            questionText.setTextSize(40);
            questionText.setTextColor(textColor);
            questionContainer.addView(questionText);
        } else if (sutraNumber == 9) {
            questionText.setVisibility(View.VISIBLE);
            // Show quadratic equation: x² + (a+b)x + (ab) = 0
            questionText.setText(String.format(Locale.getDefault(), "x² + %dx + %d = 0", (a+b), (a*b)));
            questionText.setTextSize(32);
            questionText.setTextColor(textColor);
            questionContainer.addView(questionText);
        } else if (sutraNumber == 11) {
            questionText.setVisibility(View.VISIBLE);
            // Show verification: (x+a)(x+b) = x² + (a+b)x + ab
            String expression = String.format(Locale.getDefault(), "(x+%d)(x+%d) = x² + %dx + %d", a, b, (a+b), (a*b));
            questionText.setText(expression);
            questionText.setTextSize(24);
            questionText.setTextColor(textColor);
            questionContainer.addView(questionText);
        } else if (sutraNumber == 12) {
            questionText.setVisibility(View.VISIBLE);
            questionText.setText(String.format(Locale.getDefault(), "Find 1 ÷ %d (Decimal form)", b));
            questionText.setTextSize(32);
            questionText.setTextColor(textColor);
            questionContainer.addView(questionText);
        } else if (sutraNumber == 13) {
            questionText.setVisibility(View.VISIBLE);
            questionText.setText(String.format(Locale.getDefault(), "1/(x+%d) + 1/(x+%d) = 0", a, b));
            questionText.setTextSize(24);
            questionText.setTextColor(textColor);
            questionContainer.addView(questionText);
        } else if (sutraNumber == 14) {
            questionText.setVisibility(View.VISIBLE);
            questionText.setText(a + " × " + b);
            questionText.setTextSize(40);
            questionText.setTextColor(textColor);
            questionContainer.addView(questionText);
        } else if (sutraNumber == 16) {
            questionText.setVisibility(View.VISIBLE);
            // Show polynomial: (x+a)(x+b) = x² + (a+b)x + ab
            String expr = String.format(Locale.getDefault(), "(x+%d)(x+%d) = x² + %dx + %d", a, b, (a+b), (a*b));
            questionText.setText(expr);
            questionText.setTextSize(22);
            questionText.setTextColor(textColor);
            questionContainer.addView(questionText);
        } else {
            questionText.setVisibility(View.VISIBLE);
            questionText.setText(a + ( (a==b && (sutraNumber==1 || sutraNumber==10 || sutraNumber==6)) ? "²" : " × " + b));
            questionText.setTextSize(40);
            questionText.setTextColor(textColor);
            questionContainer.addView(questionText);
        }
        
        if (!isDemo) {
            hintText.setText("Solve using the Vedic technique.");
            if (sutraNumber == 7) {
                hintText.setText("Find the value of x + y");
                intermediateTarget = (c + d) / (a + b);
            } else {
                intermediateTarget = correctAnswer;
            }
            return;
        }

        switch (sutraNumber) {
            case 1: // Ekadhikena
                if (currentStep == 0) {
                    int tens = a / 10;
                    hintText.setText("Step 1: Multiply tens digit (" + tens + ") by " + (tens + 1));
                    intermediateTarget = tens * (tens + 1);
                } else {
                    hintText.setText("Step 2: The final part is always 25. Combine it with " + (a/10 * (a/10+1)));
                    intermediateTarget = correctAnswer;
                }
                break;
            case 2: // Nikhilam
                int defA = 100 - a;
                int defB = 100 - b;
                if (currentStep == 0) {
                    hintText.setText("Step 1: Multiply deficiencies (" + defA + " × " + defB + ")");
                    intermediateTarget = defA * defB;
                } else if (currentStep == 1) {
                    hintText.setText("Step 2: Cross subtract (" + a + " - " + defB + ")");
                    intermediateTarget = a - defB;
                } else {
                    hintText.setText("Step 3: Combine left part (" + (a - defB) + ") and right part (" + String.format(Locale.getDefault(), "%02d", (defA * defB)) + ")");
                    intermediateTarget = correctAnswer;
                }
                break;
            case 3: // Urdhva (2x2 or 3x2)
                if (a < 100) {
                    int a1 = a / 10, a0 = a % 10;
                    int b1 = b / 10, b0 = b % 10;
                    if (currentStep == 0) {
                        hintText.setText("Step 1: Vertical Units (" + a0 + " × " + b0 + ").");
                        intermediateTarget = a0 * b0;
                    } else if (currentStep == 1) {
                        hintText.setText("Step 2: Crosswise Sum (" + a1 + "×" + b0 + " + " + a0 + "×" + b1 + ").\nThat's " + (a1 * b0) + " + " + (a0 * b1));
                        intermediateTarget = (a1 * b0) + (a0 * b1);
                    } else if (currentStep == 2) {
                        hintText.setText("Step 3: Vertical Tens (" + a1 + " × " + b1 + ").");
                        intermediateTarget = a1 * b1;
                    } else {
                        hintText.setText("Step 4: Combine using Carry Rule. Final Answer?");
                        intermediateTarget = correctAnswer;
                    }
                } else {
                    // 3x2 logic: (a2 a1 a0) x (b1 b0)
                    int a2 = a / 100, a1 = (a / 10) % 10, a0 = a % 10;
                    int b1 = b / 10, b0 = b % 10;
                    if (currentStep == 0) {
                        hintText.setText("Step 1: Units (" + a0 + " × " + b0 + ")");
                        intermediateTarget = a0 * b0;
                    } else if (currentStep == 1) {
                        hintText.setText("Step 2: Cross (" + a1 + "×" + b0 + " + " + a0 + "×" + b1 + ")");
                        intermediateTarget = (a1 * b0) + (a0 * b1);
                    } else if (currentStep == 2) {
                        hintText.setText("Step 3: Cross (" + a2 + "×" + b0 + " + " + a1 + "×" + b1 + ")");
                        intermediateTarget = (a2 * b0) + (a1 * b1);
                    } else if (currentStep == 3) {
                        hintText.setText("Step 4: Leftmost (" + a2 + " × " + b1 + ")");
                        intermediateTarget = a2 * b1;
                    } else {
                        hintText.setText("Step 5: Final Result (Combine with Carry Rule)");
                        intermediateTarget = correctAnswer;
                    }
                }
                break;
            case 4: // Paravartya
                int divisor4 = b;
                int digit4 = b % 10;
                int h1 = a / 100;
                int h2 = (a / 10) % 10;
                int h3 = a % 10;
                String negHint = " " + getString(R.string.negative_hint);
                
                if (currentStep == 0) {
                    hintText.setText(getString(R.string.sutra4_intro, divisor4) + negHint);
                    intermediateTarget = -digit4;
                } else if (currentStep == 1) {
                    hintText.setText(getString(R.string.sutra4_step1, h1));
                    intermediateTarget = h1;
                } else if (currentStep == 2) {
                    hintText.setText(getString(R.string.sutra4_step2, h1, -digit4, h2) + negHint);
                    intermediateTarget = h1 * (-digit4) + h2;
                } else if (currentStep == 3) {
                    int lastQ = h1 * (-digit4) + h2;
                    hintText.setText(getString(R.string.sutra4_step3, lastQ, -digit4, h3) + negHint);
                    intermediateTarget = lastQ * (-digit4) + h3;
                } else {
                    int q1 = h1;
                    int q2 = h1 * (-digit4) + h2;
                    hintText.setText(getString(R.string.sutra4_final, q1, q2));
                    intermediateTarget = a / b;
                }
                break;
            case 5: // Shunyam
                if (currentStep == 0) {
                    hintText.setText("Step 1: Calculate product of constants " + a + " × " + b);
                    intermediateTarget = a * b;
                } else {
                    hintText.setText("Step 2: Since " + a + "×" + b + " = " + c + "×" + d + ", what is x?");
                    intermediateTarget = 0;
                }
                break;
            case 6: // Anurupyena
                int subBase6 = (a / 10) * 10;
                int diff6 = a - subBase6;
                if (currentStep == 0) {
                    hintText.setText("Step 1: Multiply (" + a + " + " + diff6 + ") by " + (subBase6/10));
                    intermediateTarget = (a + diff6) * (subBase6/10);
                } else {
                    hintText.setText("Step 2: Append " + (diff6*diff6) + " to " + intermediateTarget);
                    intermediateTarget = correctAnswer;
                }
                break;
            case 7: // Sankalana
                if (currentStep == 0) {
                    hintText.setText("Step 1: Add equations and find (x + y). Hint: (c+d)/(a+b)");
                    intermediateTarget = (c + d) / (a + b);
                } else if (currentStep == 1) {
                    hintText.setText("Step 2: Subtract equations and find (x - y). Hint: (c-d)/(a-b)");
                    intermediateTarget = (c - d) / (a - b);
                } else {
                    hintText.setText("Step 3: Find x by adding (x+y) and (x-y) and dividing by 2");
                    intermediateTarget = ((c + d) / (a + b) + (c - d) / (a - b)) / 2;
                }
                break;
            case 8: // Puranapuranabhyam
                int completion = ((a / 10) + 1) * 10;
                int needed = completion - a;
                if (currentStep == 0) {
                    hintText.setText(getString(R.string.sutra8_step1, a, completion));
                    intermediateTarget = needed;
                } else {
                    hintText.setText(getString(R.string.sutra8_step2, b, completion));
                    intermediateTarget = correctAnswer;
                }
                break;
            case 9: // Chalana
                if (currentStep == 0) {
                    hintText.setText(String.format(Locale.getDefault(), "Factorize: Look for two numbers that add to %d and multiply to %d. Larger number?", (a+b), (a*b)));
                    intermediateTarget = Math.max(a, b);
                } else {
                    hintText.setText(String.format(Locale.getDefault(), "Since (x+%d)(x+%d) = 0, find the root other than -%d.", a, b, Math.max(a,b)));
                    intermediateTarget = -Math.min(a, b);
                }
                break;
            case 11: // Vyashtisamanstih
                if (currentStep == 0) {
                    hintText.setText(String.format(Locale.getDefault(), "Step 1: Find the Sum of Coefficients (Sc) for the factor (x+%d). (Set x=1)", a));
                    intermediateTarget = 1 + a;
                } else if (currentStep == 1) {
                    hintText.setText(String.format(Locale.getDefault(), "Step 2: Find the Sc for the second factor (x+%d). (Set x=1)", b));
                    intermediateTarget = 1 + b;
                } else {
                    hintText.setText("Step 3: What is the Sc of the total expression x² + " + (a+b) + "x + " + (a*b) + "?");
                    intermediateTarget = 1 + (a + b) + (a * b);
                }
                break;
            case 12: // Shesanyankena
                int auxiliaryDivisor = (b + 1) / 10;
                if (currentStep == 0) {
                    hintText.setText(String.format(Locale.getDefault(), "Step 1: For divisor %d, what is the auxiliary divisor? (One more than the first digit)", b));
                    intermediateTarget = auxiliaryDivisor;
                } else if (currentStep == 1) {
                    hintText.setText(String.format(Locale.getDefault(), "Step 2: Divide 10 (1 carried) by %d. What is the quotient?", auxiliaryDivisor));
                    intermediateTarget = 10 / auxiliaryDivisor;
                } else {
                    hintText.setText("Step 3: This quotient becomes the next digit. What is the remainder of 10 / " + auxiliaryDivisor + "?");
                    intermediateTarget = 10 % auxiliaryDivisor;
                }
                break;
            case 13: // Sopantyadvayamantyam
                if (currentStep == 0) {
                    hintText.setText(String.format(Locale.getDefault(), "Step 1: Find the sum of the constants (%d + %d).", a, b));
                    intermediateTarget = a + b;
                } else {
                    hintText.setText(String.format(Locale.getDefault(), "Step 2: The sum of denominators is zero: 2x + %d = 0. Find x.", (a + b)));
                    intermediateTarget = -(a + b) / 2;
                }
                break;
            case 10: // Yavadunam
                int def10 = 100 - a;
                if (currentStep == 0) {
                    hintText.setText(getString(R.string.sutra10_step1, a, def10));
                    intermediateTarget = a - def10;
                } else if (currentStep == 1) {
                    hintText.setText(getString(R.string.sutra10_step2, def10));
                    intermediateTarget = def10 * def10;
                } else {
                    hintText.setText(getString(R.string.sutra10_step3));
                    intermediateTarget = correctAnswer;
                }
                break;
            case 14: // Ekanyunena
                if (currentStep == 0) {
                    hintText.setText(String.format(Locale.getDefault(), "Step 1 (Left Part): Subtract 1 from %d.", a));
                    intermediateTarget = a - 1;
                } else if (currentStep == 1) {
                    int leftPart = a - 1;
                    hintText.setText(String.format(Locale.getDefault(), "Step 2 (Right Part): Find the complement of %d (99 - %d).", leftPart, leftPart));
                    intermediateTarget = 99 - leftPart;
                } else {
                    hintText.setText("Step 3: Combine parts. Final Answer?");
                    intermediateTarget = correctAnswer;
                }
                break;
            case 15: // Gunakasamuccayah
                if (currentStep == 0) {
                    hintText.setText(getString(R.string.sutra15_step1, a));
                    intermediateTarget = getDigitSum(a);
                } else {
                    int dsA = getDigitSum(a);
                    int dsB = getDigitSum(b);
                    hintText.setText(getString(R.string.sutra15_step2));
                    intermediateTarget = getDigitSum(dsA * dsB);
                }
                break;
            case 16: // Gunita Samuccayah (Verification of polynomial factors)
                int sc1 = 1 + a;
                int sc2 = 1 + b;
                int totalSc = 1 + (a + b) + (a * b);
                if (currentStep == 0) {
                    hintText.setText(String.format(Locale.getDefault(), "Step 1: Find Sum of Coefficients (Sc) for (x + %d). (Let x=1)", a));
                    intermediateTarget = sc1;
                } else if (currentStep == 1) {
                    hintText.setText(String.format(Locale.getDefault(), "Step 2: Find Sc for (x + %d). (Let x=1)", b));
                    intermediateTarget = sc2;
                } else {
                    hintText.setText(String.format(Locale.getDefault(), "Step 3: Sc(Product) = Sc(f1) × Sc(f2) = %d × %d. What is the total Sc?", sc1, sc2));
                    intermediateTarget = totalSc;
                }
                break;
            default:
                hintText.setText("Calculate the result");
                intermediateTarget = correctAnswer;
        }
    }

    private void checkAnswer() {
        String input = answerInput.getText().toString().trim();
        if (input.isEmpty()) return;

        try {
            int userAns = Integer.parseInt(input);
            if (userAns == intermediateTarget) {
                hideKeyboard();
                animateTransition();
                if (currentStep < maxSteps - 1) {
                    solutionText.setText("✅ Correct! Now for the next step.");
                    solutionText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    currentStep++;
                    loadStep();
                } else {
                    solutionText.setText("✨ Brilliant! Final Answer Correct.");
                    solutionText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    checkBtn.setVisibility(View.GONE);
                    answerInput.setVisibility(View.GONE);
                    nextBtn.setVisibility(View.VISIBLE);
                    nextBtn.setText("Next Problem");
                    
                    updateUserProgress();
                }
            } else {
                solutionText.setText("❌ Try again! Focus on the hint.");
                solutionText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        } catch (NumberFormatException e) {
            solutionText.setText("Please enter a valid number");
        }
    }

    private void animateTransition() {
        questionContainer.setAlpha(0.4f);
        questionContainer.animate().alpha(1f).setDuration(400).start();
        hintText.setTranslationX(100f);
        hintText.animate().translationX(0f).setDuration(400).start();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void loadInitialProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        long currentSutra = doc.getLong("currentSutra") != null ? doc.getLong("currentSutra") : 1;
                        if (sutraNumber == currentSutra) {
                            int progress = doc.getLong("levelProgress") != null ? doc.getLong("levelProgress").intValue() : 0;
                            updateProgressUI(progress);
                        } else if (sutraNumber < currentSutra) {
                            updateProgressUI(100);
                        } else {
                            updateProgressUI(0);
                        }
                    }
                });
    }

    private void updateProgressUI(int progress) {
        levelProgressBar.setProgress(progress);
        progressText.setText(progress + "% complete");
    }

    private int getDigitSum(int n) {
        int sum = 0;
        n = Math.abs(n);
        while (n > 0 || sum > 9) {
            if (n == 0) {
                n = sum;
                sum = 0;
            }
            sum += n % 10;
            n /= 10;
        }
        return sum;
    }

    private void showHappyToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_happy, findViewById(android.R.id.content), false);

        TextView text = layout.findViewById(R.id.toastMessage);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void triggerConfetti() {
        EmitterConfig emitterConfig = new Emitter(100L, TimeUnit.MILLISECONDS).max(100);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .spread(360)
                        .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE))
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(0f, 30f)
                        .position(0.5, 0.3)
                        .build()
        );
    }

    private void updateUserProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        long currentSutra = doc.getLong("currentSutra") != null ? doc.getLong("currentSutra") : 1;
                        
                        // Only update progress if playing the currently active (highest) level
                        if (sutraNumber == currentSutra) {
                            int progress = doc.getLong("levelProgress") != null ? doc.getLong("levelProgress").intValue() : 0;
                            progress += 20; // 5 problems to complete level

                            if (progress >= 100) {
                                db.collection("users").document(user.getUid())
                                        .update("currentSutra", FieldValue.increment(1),
                                                "levelProgress", 0,
                                                "xp", FieldValue.increment(50));
                                updateProgressUI(100);
                                triggerConfetti();
                                showHappyToast(getString(R.string.sutra_mastered));
                            } else {
                                db.collection("users").document(user.getUid())
                                        .update("levelProgress", progress,
                                                "xp", FieldValue.increment(10));
                                updateProgressUI(progress);
                                // Small toast for problem completion
                                Toast.makeText(this, "Great job! +10 XP", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
