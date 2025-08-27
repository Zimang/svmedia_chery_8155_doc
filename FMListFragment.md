isSearching ->

startLoadingAni

onResume->
startScanIfNeed->
startScan(false) ->
{
processCommand(RadioAction.SPECIFIES_AST, ChangeReasonData.CLICK: ChangeReasonData.UI_START, mGetRadioStatusTool.getFMRadioMessage());
Log.d(TAG, "startScan: first scan")
}->

