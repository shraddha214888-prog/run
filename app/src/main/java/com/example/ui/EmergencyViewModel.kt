package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AmbulanceAudioController
import com.example.data.local.CivicClearanceRecord
import com.example.data.local.EmergencyDatabase
import com.example.data.local.EmergencyMission
import com.example.data.model.CivilianAlertLevel
import com.example.data.model.CivilianAlertStatus
import com.example.data.model.EmergencyCase
import com.example.data.model.EmergencyPriority
import com.example.data.model.GeoPoint
import com.example.data.model.Hospital
import com.example.data.model.NavigationStep
import com.example.data.model.SignalState
import com.example.data.model.StepIconType
import com.example.data.model.TrafficSignalJunction
import com.example.location.GpsLocationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class EmergencyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = EmergencyDatabase.getDatabase(application)
    private val missionDao = db.missionDao()
    val gpsManager = GpsLocationManager(application)
    val audioController = AmbulanceAudioController(application)

    // Current Active Mode in App
    // "AMBULANCE" (ડ્રાઇવર મોડ), "CIVILIAN" (વાહનચાલક સ્પેસ એલર્ટ), "TRAFFIC_CONTROL" (સિગ્નલ કંટ્રોલ રૂમ), "HOSPITALS" (હોસ્પિટલ યાદી)
    private val _selectedTab = MutableStateFlow("AMBULANCE")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    // Emergency Mission State
    private val _isMissionActive = MutableStateFlow(true)
    val isMissionActive: StateFlow<Boolean> = _isMissionActive.asStateFlow()

    private val _ambulanceProgress = MutableStateFlow(0.25f)
    val ambulanceProgress: StateFlow<Float> = _ambulanceProgress.asStateFlow()

    private val _ambulanceSpeed = MutableStateFlow(62)
    val ambulanceSpeed: StateFlow<Int> = _ambulanceSpeed.asStateFlow()

    private val _isSirenMuted = MutableStateFlow(false)
    val isSirenMuted: StateFlow<Boolean> = _isSirenMuted.asStateFlow()

    // Emergency Cases (કટોકટીના પ્રકાર)
    val emergencyCases = listOf(
        EmergencyCase(
            id = "CASE_CARDIAC",
            titleGujarati = "કાર્ડિયાક એરેસ્ટ (હૃદયરોગ કટોકટી)",
            titleEnglish = "Cardiac Arrest",
            priority = EmergencyPriority.CRITICAL,
            descriptionGujarati = "દર્દીને તાત્કાલિક કેથલેબ અને કાર્ડિયાક આઇસીયુની જરૂર છે.",
            recommendedFacility = "યુ. એન. મહેતા ઇન્સ્ટીટ્યુટ"
        ),
        EmergencyCase(
            id = "CASE_TRAUMA",
            titleGujarati = "ગંભીર માર્ગ અકસ્માત (ટ્રોમા)",
            titleEnglish = "Severe Trauma Accident",
            priority = EmergencyPriority.CRITICAL,
            descriptionGujarati = "મલ્ટીપલ ઇન્જરી અને લોહી વહી જવાની સ્થિતિ.",
            recommendedFacility = "સિવિલ હોસ્પિટલ ટ્રોમા સેન્ટર"
        ),
        EmergencyCase(
            id = "CASE_MATERNITY",
            titleGujarati = "જટિલ ડિલિવરી / પ્રસુતિ",
            titleEnglish = "Complicated Maternity",
            priority = EmergencyPriority.HIGH,
            descriptionGujarati = "તાત્કાલિક સિઝેરિયન ઓપરેશન જરૂરિયાત.",
            recommendedFacility = "એસ.વી.પી. હોસ્પિટલ"
        ),
        EmergencyCase(
            id = "CASE_RESPIRATORY",
            titleGujarati = "ગંભીર શ્વાસની તકલીફ / ICU",
            titleEnglish = "Severe Respiratory Distress",
            priority = EmergencyPriority.HIGH,
            descriptionGujarati = "વેન્ટિલેટર અને ઉચ્ચ ઓક્સિજન સપોર્ટ જરૂરી.",
            recommendedFacility = "ઝાયડસ હોસ્પિટલ"
        )
    )

    private val _selectedCase = MutableStateFlow(emergencyCases[0])
    val selectedCase: StateFlow<EmergencyCase> = _selectedCase.asStateFlow()

    // Hospital Directory (ગુજરાતના મુખ્ય હોસ્પિટલો)
    val initialHospitals = listOf(
        Hospital(
            id = "HOSP_CIVIL",
            nameGujarati = "અમદાવાદ સિવિલ હોસ્પિટલ (ટ્રોમા સેન્ટર)",
            nameEnglish = "Ahmedabad Civil Hospital & Trauma Centre",
            addressGujarati = "અસારવા, અમદાવાદ",
            latitude = 23.0528,
            longitude = 72.5986,
            totalIcuBeds = 120,
            availableIcuBeds = 18,
            traumaLevel = "લેવલ-૧ ટ્રોમા",
            phone = "079-22683721",
            specializedUnits = listOf("ન્યુરોસર્જરી", "ટ્રોમા કેર", "બર્ન્સ વોર્ડ"),
            distanceKm = 4.2f,
            estimatedMinutes = 7
        ),
        Hospital(
            id = "HOSP_UN_MEHTA",
            nameGujarati = "યુ. એન. મહેતા ઇન્સ્ટિટ્યૂટ ઓફ કાર્ડિયોલોજી",
            nameEnglish = "U. N. Mehta Institute of Cardiology",
            addressGujarati = "સિવિલ હોસ્પિટલ કેમ્પસ, અસારવા",
            latitude = 23.0535,
            longitude = 72.5992,
            totalIcuBeds = 85,
            availableIcuBeds = 14,
            traumaLevel = "સ્પેશિયાલિટી હાર્ટ સેન્ટર",
            phone = "079-22684200",
            specializedUnits = listOf("કેથલેબ", "હાર્ટ ટ્રાન્સપ્લાન્ટ", "ઇસીએમઓ"),
            distanceKm = 4.5f,
            estimatedMinutes = 8
        ),
        Hospital(
            id = "HOSP_SVP",
            nameGujarati = "સરદાર વલ્લભભાઈ પટેલ (SVP) હોસ્પિટલ",
            nameEnglish = "Sardar Vallabhbhai Patel (SVP) Hospital",
            addressGujarati = "એલિસબ્રિજ, સાબરમતી રિવરફ્રન્ટ",
            latitude = 23.0189,
            longitude = 72.5714,
            totalIcuBeds = 90,
            availableIcuBeds = 22,
            traumaLevel = "મલ્ટી-સ્પેશિયાલિટી સુપર કેર",
            phone = "079-26577621",
            specializedUnits = listOf("ઈમરજન્સી મેડિસિન", "ડાયાલિસિસ", "ઓન્કોલોજી"),
            distanceKm = 3.1f,
            estimatedMinutes = 5
        ),
        Hospital(
            id = "HOSP_ZYDUS",
            nameGujarati = "ઝાયડસ હોસ્પિટલ",
            nameEnglish = "Zydus Hospital",
            addressGujarati = "થલતેજ, એસ.જી. હાઇવે",
            latitude = 23.0610,
            longitude = 72.5115,
            totalIcuBeds = 60,
            availableIcuBeds = 9,
            traumaLevel = "પ્રાઇવેટ ક્રિટિકલ કેર",
            phone = "079-66190201",
            specializedUnits = listOf("ઓર્થોપેડિક ટ્રોમા", "કાર્ડિયોવાસ્ક્યુલર"),
            distanceKm = 5.8f,
            estimatedMinutes = 10
        ),
        Hospital(
            id = "HOSP_APOLLO",
            nameGujarati = "એપોલો હોસ્પિટલ્સ ગાંધીનગર",
            nameEnglish = "Apollo Hospitals International",
            addressGujarati = "ઇન્દિરા બ્રિજ, ગાંધીનગર હાઇવે",
            latitude = 23.1098,
            longitude = 72.6052,
            totalIcuBeds = 70,
            availableIcuBeds = 12,
            traumaLevel = "નેશનલ એક્રેડિટેડ ટ્રોમા",
            phone = "079-66701800",
            specializedUnits = listOf("ઓર્ગન ટ્રાન્સપ્લાન્ટ", "સ્ટ્રોક સેન્ટર"),
            distanceKm = 8.4f,
            estimatedMinutes = 14
        )
    )

    private val _hospitals = MutableStateFlow(initialHospitals)
    val hospitals: StateFlow<List<Hospital>> = _hospitals.asStateFlow()

    private val _selectedHospital = MutableStateFlow(initialHospitals[0])
    val selectedHospital: StateFlow<Hospital> = _selectedHospital.asStateFlow()

    // Smart Traffic Signals (કોરિડોર સ્માર્ટ સિગ્નલો)
    val initialSignals = listOf(
        TrafficSignalJunction(
            id = "SIG_1",
            nameGujarati = "ઇસ્કોન ચાર રસ્તા (એસ.જી. હાઇવે)",
            nameEnglish = "Iskcon Cross Roads Junction",
            latitude = 23.0285,
            longitude = 72.5068,
            currentState = SignalState.PREEMPTED_GREEN,
            normalCycleSeconds = 60,
            preemptionActive = true,
            secondsRemaining = 24,
            distanceFromAmbulanceMeters = 180f,
            vehiclesClearedCount = 38
        ),
        TrafficSignalJunction(
            id = "SIG_2",
            nameGujarati = "પકવાન ક્રોસ રોડ",
            nameEnglish = "Pakwan Dining Junction",
            latitude = 23.0365,
            longitude = 72.5140,
            currentState = SignalState.PREEMPTED_GREEN,
            normalCycleSeconds = 60,
            preemptionActive = true,
            secondsRemaining = 35,
            distanceFromAmbulanceMeters = 420f,
            vehiclesClearedCount = 52
        ),
        TrafficSignalJunction(
            id = "SIG_3",
            nameGujarati = "શિવરંજની ચાર રસ્તા",
            nameEnglish = "Shivranjani Cross Roads",
            latitude = 23.0234,
            longitude = 72.5305,
            currentState = SignalState.YELLOW,
            normalCycleSeconds = 45,
            preemptionActive = false,
            secondsRemaining = 8,
            distanceFromAmbulanceMeters = 890f,
            vehiclesClearedCount = 14
        ),
        TrafficSignalJunction(
            id = "SIG_4",
            nameGujarati = "નહેરુનગર સર્કલ",
            nameEnglish = "Nehrunagar Circle",
            latitude = 23.0210,
            longitude = 72.5450,
            currentState = SignalState.RED,
            normalCycleSeconds = 60,
            preemptionActive = false,
            secondsRemaining = 32,
            distanceFromAmbulanceMeters = 1450f,
            vehiclesClearedCount = 0
        ),
        TrafficSignalJunction(
            id = "SIG_5",
            nameGujarati = "સિવિલ હોસ્પિટલ કેમ્પસ ગેટ",
            nameEnglish = "Civil Hospital Campus Gate",
            latitude = 23.0510,
            longitude = 72.5950,
            currentState = SignalState.RED,
            normalCycleSeconds = 45,
            preemptionActive = false,
            secondsRemaining = 15,
            distanceFromAmbulanceMeters = 2900f,
            vehiclesClearedCount = 0
        )
    )

    private val _signals = MutableStateFlow(initialSignals)
    val signals: StateFlow<List<TrafficSignalJunction>> = _signals.asStateFlow()

    // Navigation Steps (વળાંક નિર્દેશો)
    val navigationSteps = listOf(
        NavigationStep(
            stepNumber = 1,
            instructionGujarati = "એસ.જી. હાઇવે પર સીધા ૨.૨ કિમી આગળ વધો",
            instructionEnglish = "Proceed straight on S.G. Highway for 2.2 km",
            distanceMeters = 2200,
            iconType = StepIconType.STRAIGHT
        ),
        NavigationStep(
            stepNumber = 2,
            instructionGujarati = "ઇસ્કોન જંક્શન સિગ્નલ ક્લિયર કરાયું છે, ગ્રીન વેવ ચાલુ છે",
            instructionEnglish = "Iskcon Junction is cleared with Green Wave",
            distanceMeters = 180,
            iconType = StepIconType.CROSS_JUNCTION
        ),
        NavigationStep(
            stepNumber = 3,
            instructionGujarati = "આગળ શિવરંજની તરફ ડાબી બાજુ વળો",
            instructionEnglish = "Turn left towards Shivranjani",
            distanceMeters = 900,
            iconType = StepIconType.TURN_LEFT
        ),
        NavigationStep(
            stepNumber = 4,
            instructionGujarati = "સિવિલ હોસ્પિટલ ટ્રોમા કેરમાં આગમન (ઈમરજન્સી બે એન્ટ્રી)",
            instructionEnglish = "Arrival at Civil Hospital Trauma Center",
            distanceMeters = 4200,
            iconType = StepIconType.HOSPITAL_ARRIVAL
        )
    )

    // Civilian Commuter Alert Status (વાહનચાલક ચેતવણી સ્થિતિ)
    private val _civilianAlert = MutableStateFlow(
        CivilianAlertStatus(
            alertLevel = CivilianAlertLevel.PREPARATION,
            ambulanceDistanceMeters = 280f,
            ambulanceSpeedKmh = 62,
            instructionGujarati = "સાવધાન: પાછળથી ૧૦૮ એમ્બ્યુલન્સ આવી રહી છે! કૃપા કરીને ડાબી બાજુ વાહન વાળો અને લેન ખાલી કરો.",
            instructionEnglish = "Ambulance approaching from behind! Move left to give way.",
            relativeBearingDegrees = 180f,
            corridorCleared = false
        )
    )
    val civilianAlert: StateFlow<CivilianAlertStatus> = _civilianAlert.asStateFlow()

    // Civic Badges & Points
    val totalCivicPoints = missionDao.getTotalCivicPoints().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        150
    )

    val missionHistory = missionDao.getAllMissions().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private var simulationJob: Job? = null

    init {
        startSimulationLoop()
        startAudioAlerts()
    }

    private fun startAudioAlerts() {
        if (!_isSirenMuted.value) {
            audioController.startSiren(viewModelScope)
        }
    }

    private fun startSimulationLoop() {
        simulationJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)

                // 1. Advance Ambulance Progress along the corridor
                if (_isMissionActive.value) {
                    var newProgress = _ambulanceProgress.value + 0.012f
                    if (newProgress >= 0.98f) {
                        newProgress = 0.05f // Loop back for continuous demonstration
                    }
                    _ambulanceProgress.value = newProgress

                    // Distance to target hospital reduces
                    val totalDistanceKm = 4.5f
                    val remainingDistance = (totalDistanceKm * (1f - newProgress)).coerceAtLeast(0.1f)
                    val remainingMins = ((remainingDistance / 55f) * 60).toInt().coerceAtLeast(1)

                    _selectedHospital.value = _selectedHospital.value.copy(
                        distanceKm = (remainingDistance * 10).toInt() / 10f,
                        estimatedMinutes = remainingMins
                    )

                    // 2. Dynamic Smart Traffic Signal Preemption
                    // As ambulance approaches within 450m, preemption triggers green wave
                    val updatedSignals = _signals.value.mapIndexed { index, sig ->
                        val sigTargetDistance = ((index + 1) * 600f) - (newProgress * 3000f)
                        val dist = sigTargetDistance.coerceAtLeast(20f)
                        val isWithinPreemptZone = dist <= 500f && dist >= 20f

                        val state = if (isWithinPreemptZone) {
                            SignalState.PREEMPTED_GREEN
                        } else if (dist < 20f) {
                            SignalState.GREEN
                        } else {
                            if (sig.currentState == SignalState.PREEMPTED_GREEN) SignalState.YELLOW else sig.currentState
                        }

                        val remaining = if (isWithinPreemptZone) {
                            (dist / 15).toInt().coerceIn(5, 40)
                        } else {
                            (sig.secondsRemaining - 1).let { if (it <= 0) sig.normalCycleSeconds else it }
                        }

                        sig.copy(
                            distanceFromAmbulanceMeters = dist,
                            currentState = state,
                            preemptionActive = isWithinPreemptZone,
                            secondsRemaining = remaining,
                            vehiclesClearedCount = if (isWithinPreemptZone) sig.vehiclesClearedCount + 1 else sig.vehiclesClearedCount
                        )
                    }
                    _signals.value = updatedSignals

                    // 3. Update Civilian Proximity Alert
                    val commuterDistance = ((1f - (newProgress % 0.35f) / 0.35f) * 600f).coerceAtLeast(40f)
                    val level = when {
                        commuterDistance < 150f -> CivilianAlertLevel.IMMEDIATE_ACTION
                        commuterDistance < 350f -> CivilianAlertLevel.PREPARATION
                        commuterDistance < 700f -> CivilianAlertLevel.AWARENESS
                        else -> CivilianAlertLevel.NORMAL
                    }

                    val gujInstruction = when (level) {
                        CivilianAlertLevel.IMMEDIATE_ACTION ->
                            "🚨 અતિ તાકીદનું: એમ્બ્યુલન્સ ફક્ત ${commuterDistance.toInt()} મીટર પાછળ છે! તુરંત ડાબી લેનમાં ઊભા રહો!"
                        CivilianAlertLevel.PREPARATION ->
                            "⚠️ સાવધાન: ૧૦૮ એમ્બ્યુલન્સ ${commuterDistance.toInt()} મીટર પાછળ આવી રહી છે. ડાબી બાજુ ખસીને રસ્તો ખાલી કરો."
                        CivilianAlertLevel.AWARENESS ->
                            "📢 સૂચના: એમ્બ્યુલન્સ કોરિડોર સક્રિય છે (અંતર: ${commuterDistance.toInt()} મીટર)."
                        CivilianAlertLevel.NORMAL ->
                            "રસ્તો સામાન્ય સ્થિતિમાં છે."
                    }

                    _civilianAlert.value = _civilianAlert.value.copy(
                        alertLevel = level,
                        ambulanceDistanceMeters = commuterDistance,
                        instructionGujarati = gujInstruction
                    )

                    // Trigger Haptic Warning when close
                    if (level == CivilianAlertLevel.IMMEDIATE_ACTION) {
                        audioController.triggerEmergencyHaptics()
                    }
                }
            }
        }
    }

    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }

    fun selectCase(emergencyCase: EmergencyCase) {
        _selectedCase.value = emergencyCase
        // Voice alert in Gujarati
        audioController.speakGujarati("નવો કેસ નોંધાયો: ${emergencyCase.titleGujarati}")
    }

    fun selectHospital(hospital: Hospital) {
        _selectedHospital.value = hospital
        audioController.speakGujarati("લક્ષ્ય હોસ્પિટલ: ${hospital.nameGujarati}")
    }

    fun toggleSiren() {
        val newState = !_isSirenMuted.value
        _isSirenMuted.value = newState
        if (newState) {
            audioController.stopSiren()
        } else {
            audioController.startSiren(viewModelScope)
        }
    }

    fun forceGreenSignal(signalId: String) {
        _signals.value = _signals.value.map { sig ->
            if (sig.id == signalId) {
                sig.copy(
                    currentState = SignalState.PREEMPTED_GREEN,
                    preemptionActive = true,
                    secondsRemaining = 45
                )
            } else {
                sig
            }
        }
        audioController.speakGujarati("સિગ્નલ ગ્રીન વેવ સક્રિય કરવામાં આવ્યું છે.")
    }

    fun civilianGiveWay() {
        _civilianAlert.value = _civilianAlert.value.copy(corridorCleared = true)
        audioController.speakGujarati("આભાર! તમે એમ્બ્યુલન્સને રસ્તો આપ્યો.")

        // Award points in Room database
        viewModelScope.launch {
            missionDao.insertCivicRecord(
                CivicClearanceRecord(
                    timestamp = System.currentTimeMillis(),
                    ambulanceCode = "GJ-01-AMB-108",
                    locationNameGujarati = "એસ.જી. હાઇવે કોરિડોર",
                    pointsAwarded = 25,
                    reactionTimeSeconds = 3
                )
            )
        }
    }

    fun completeMission() {
        viewModelScope.launch {
            val mission = EmergencyMission(
                missionCode = "108-EMRG-${(1000..9999).random()}",
                timestamp = System.currentTimeMillis(),
                emergencyType = _selectedCase.value.titleGujarati,
                targetHospital = _selectedHospital.value.nameGujarati,
                durationSeconds = 480,
                distanceCoveredKm = 5.2f,
                trafficSignalsPreempted = _signals.value.count { it.preemptionActive },
                averageSpeedKmh = _ambulanceSpeed.value,
                status = "સફળતાપૂર્વક પૂર્ણ"
            )
            missionDao.insertMission(mission)
            audioController.speakGujarati("દર્દી હોસ્પિટલ પહોંચી ગયા છે. મિશન સફળતાપૂર્વક પૂર્ણ થયું.")
            _ambulanceProgress.value = 0.05f
        }
    }

    fun requestGpsActivation() {
        gpsManager.startLocationUpdates { loc ->
            _ambulanceSpeed.value = if (loc.hasSpeed()) (loc.speed * 3.6f).toInt().coerceAtLeast(30) else 60
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
        audioController.release()
        gpsManager.stopLocationUpdates()
    }
}
