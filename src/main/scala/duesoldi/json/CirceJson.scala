package duesoldi.json

import io.circe.{Json, JsonObject}

object CirceJson
{
  def toMap(jsonObject: JsonObject): Map[String, Any] = {
    jsonObject.toMap.collect {
      case (key, value) if !value.isNull => key -> decircefy(value)
    }
  }

  def decircefy(json: Json): Any = {
    json.fold(
      jsonNull = null,
      jsonBoolean = identity,
      jsonNumber = num => num.toInt.getOrElse(num.toDouble),
      jsonString = identity,
      jsonArray = _.map(decircefy),
      jsonObject = toMap
    )
  }
}
