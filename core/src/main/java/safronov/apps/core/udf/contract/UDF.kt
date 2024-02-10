package safronov.apps.core.udf.contract

interface UDF {

    interface State: UDF
    interface Action: UDF
    interface Effect: UDF
    interface Event: UDF

}