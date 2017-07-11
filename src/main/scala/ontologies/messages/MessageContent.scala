package ontologies.messages

sealed trait MessageContent

final case class InfoCell(id: Int,
                          uri: String,
                          name: String,
                          roomVertices: Coordinates,
                          antennaPosition: Point) extends MessageContent


final case class Point(var x: Int, var y: Int) extends MessageContent

final case class Coordinates(northWest: Point,
                             northEast: Point,
                             southWest: Point,
                             southEast: Point)

final case class Area(id: Int,
                      cells: List[Cell]) extends MessageContent

final case class Cell(infoCell: InfoCell,
                      sensors: List[Sensor],
                      neighbors: List[InfoCell],
                      passages: List[Passage],
                      isEntryPoint: Boolean,
                      isExitPoint: Boolean,
                      capacity: Int,
                      squareMeters: Double,
                      currentPeople: Int,
                      practicabilityLevel: Double) extends MessageContent


final case class Passage(neighborId: Int,
                         startCoordinates: Point,
                         endCoordinates: Point)

final case class Sensor(category: Int, value: Double)

final case class CellForUser(actorPath: String,
                             infoCell: InfoCell,
                             neighbors: List[InfoCell],
                             passages: List[Passage]) extends MessageContent

object CellForUser {
    def apply(cell: Cell, actorPath: String): CellForUser =
        new CellForUser(actorPath, cell.infoCell, cell.neighbors, cell.passages)
}

final case class CellForCell(infoCell: InfoCell,
                             neighbors: List[InfoCell],
                             passages: List[Passage],
                             isEntryPoint: Boolean,
                             isExitPoint: Boolean,
                             practicabilityLevel: Double)

object CellForCell {
    def apply(cell: Cell): CellForCell =
        new CellForCell(cell.infoCell, cell.neighbors, cell.passages, cell.isEntryPoint,
            cell.isExitPoint, cell.practicabilityLevel)
}

final case class AreaForCell(id: Int,
                             cells: List[CellForCell])

object AreaForCell {
    def apply(area: Area): AreaForCell = new AreaForCell(area.id, area.cells.map(c => CellForCell(c)))
}

final case class UpdateForAdmin(list: List[CellUpdate])

final case class CellUpdate(infoCell: InfoCell,
                            currentPeople: Int,
                            sensors: List[Sensor])

object CellUpdate {
    def apply(cell: Cell): CellUpdate = new CellUpdate(cell.infoCell, cell.currentPeople, cell.sensors)
}

