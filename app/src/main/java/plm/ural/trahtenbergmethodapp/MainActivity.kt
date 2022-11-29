package plm.ural.trahtenbergmethodapp

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.lang.NumberFormatException
import kotlin.math.pow
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    private val buttonAvailableTime = 5_000L
    private val numberVisibleTime = 15_000L
    val defaultFirstNumberLength = 2
    val defaultSecondNumberLength = 1
    private var inGame:Boolean = false
    var symbol = "*"
    var difficultyLevel = 0
    var initialTime:Long = 3*60*1_000
    var modTime:Long = initialTime
    var correctAnswer:Int = 0
    var symbolsNumFirst = defaultFirstNumberLength
    var symbolsNumSecond = defaultSecondNumberLength
    private var randFirstNumber = 0
    private var randSecondNumber = 0

    var textTimer:TextView? = null
    var answerHint:TextView? = null
    var okButton: Button? = null
    var answerInput:EditText? = null
    var firstNumberField:TextView? = null
    var secondNumberField:TextView? = null
    private var difficultyBar:SeekBar? = null
    private val rnd:Random = Random(System.currentTimeMillis())

    private var showMultyNumberTimer:CountDownTimer? = null
    var timer = mainSessionRun()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textTimer = findViewById(R.id.timer)
        changeVisibleTime(modTime)
        okButton = findViewById(R.id.okButton)
        answerInput = findViewById(R.id.answerTextNumber)
        firstNumberField = findViewById(R.id.firstNumberField)
        firstNumberField?.text = symbol.repeat(symbolsNumFirst)
        secondNumberField = findViewById(R.id.secondNumberFIeld)
        secondNumberField?.text = symbol.repeat(symbolsNumSecond)
        answerHint = findViewById(R.id.answerHint)
        difficultyBar = findViewById(R.id.seekBar2)
        symbol = resources.getString(R.string.spec_symbol)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    override fun onStart() {
        super.onStart()
        difficultyBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                difficultyLevel = p1
                symbolsNumFirst = defaultFirstNumberLength + p1/3
                symbolsNumSecond = defaultSecondNumberLength + p1/3
                firstNumberField?.text = symbol.repeat(symbolsNumFirst)
                secondNumberField?.text = symbol.repeat(symbolsNumSecond)
                modTime = initialTime - ( p1 % 3*30_000L)
                changeVisibleTime(modTime)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

//        answerInput?.addTextChangedListener(object: TextWatcher{
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                if(inGame && p1 >(correctAnswer.toString().length-1))
//                   answerInput?.setText(answerInput?.text.toString().substring(0,answerInput?.text.toString().length-1))
//            }
//            override fun afterTextChanged(p0: Editable?) {}
//        })

        okButton?.setOnClickListener {
            difficultyBar?.isEnabled = inGame
            inGame = !inGame

            if (inGame) {
                onGameStart()
            } else {
                onGameEnd()
            }
        }
    }

    fun changeVisibleTime(millis:Long){
        val minutes = millis/1000/60
        val seconds = millis/1000%60
        textTimer?.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun mainSessionRun():CountDownTimer{
        return object : CountDownTimer(modTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                changeVisibleTime(millisUntilFinished)
            }

            override fun onFinish() {
                onTimeOver()
            }
        }
    }

    private fun onGameStart(){
        answerInput?.text?.clear()
        answerHint?.text = resources.getString(R.string.empty)
        okButton?.text = resources.getString(R.string.result_btn)

        randFirstNumber = rnd.nextInt(
            10.0.pow(symbolsNumFirst.toDouble() - 1).toInt(),
            (10.0.pow((symbolsNumFirst.toDouble())) -1).toInt())
        randSecondNumber = rnd.nextInt(
            10.0.pow(symbolsNumSecond.toDouble() - 1).toInt(),
            (10.0.pow((symbolsNumSecond.toDouble())) -1).toInt())
        correctAnswer = randFirstNumber*randSecondNumber
        firstNumberField?.text = randFirstNumber.toString()
        secondNumberField?.text = randSecondNumber.toString()
        //START TIMERS
        timer.start()
        //DISABLE BUTTON
        //Логика отключения кнопки для предотвращения спама
        okButton?.isEnabled = false
        getUtilityTimer(buttonAvailableTime,1000,{},{okButton?.isEnabled = true }).start()
        //HIDE INITIAL NUMBERS
        showMultyNumberTimer = getUtilityTimer((3-difficultyLevel%3)*numberVisibleTime,
            1000, {},
            { firstNumberField?.text = symbol.repeat(symbolsNumFirst)
                secondNumberField?.text = symbol.repeat(symbolsNumSecond)}).start()

    }

    private fun onGameEnd(){
        timer.cancel()
        showMultyNumberTimer?.cancel()
        okButton?.text = resources.getString(R.string.restart_btn)
        changeVisibleTime(modTime)
        inGame = false
        val playerInput:Int = answerInput?.text.run{
            try {
                toString().toInt()
            }catch (e:NumberFormatException){
                0
            }
        }
        difficultyBar?.isEnabled = true
        showInitialNumbers()
        answerHint?.text = correctAnswer.toString()
        if(playerInput == correctAnswer)
            Toast.makeText(this@MainActivity.applicationContext,
                "Ответ правильный!", Toast.LENGTH_LONG).show()
        else if(answerInput?.text?.isEmpty() == true)
            Toast.makeText(this@MainActivity.applicationContext,
                "Ответ не был дан!", Toast.LENGTH_LONG).show()
        else Toast.makeText(this@MainActivity.applicationContext,
            "Ответ неверный!", Toast.LENGTH_LONG).show()

    }

    private fun onTimeOver(){
        changeVisibleTime(modTime)
        inGame = false
        okButton?.text = resources.getString(R.string.restart_btn)
        difficultyBar?.isEnabled = true
        answerHint?.text = correctAnswer.toString()
        showInitialNumbers()
        Toast.makeText(this@MainActivity.applicationContext,
            "Вы не успели дать правильный ответ!", Toast.LENGTH_LONG).show()
    }

    private fun showInitialNumbers(){
        if(randFirstNumber != 0) {
            firstNumberField?.text = randFirstNumber.toString()
            secondNumberField?.text = randSecondNumber.toString()
        }
    }

    private fun getUtilityTimer(overallTime:Long, countDownTime:Long,
                                onTickFunc:()->Unit, onFinishFunc: () -> Unit):CountDownTimer{
        return object : CountDownTimer(overallTime, countDownTime) {
            override fun onTick(millisUntilFinished: Long) {
                onTickFunc()
            }
            override fun onFinish() {
                onFinishFunc()
            }
        }
    }
}