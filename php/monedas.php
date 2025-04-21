<?php
$servername = "localhost";
$username = "Xlbilbao040";
$password = "AiLq63dHFf";
$dbname = "Xlbilbao040_RedJack";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Conexión fallida: " . $conn->connect_error);
}

//obtener los datos JSON del cuerpo de la petición
$inputJSON = file_get_contents("php://input");
$input = json_decode($inputJSON, true); //decodificar a un array asociativo

//ruta para sumar monedas
if ($_SERVER["REQUEST_METHOD"] === "POST" && isset($input["accion"]) && $input["accion"] === "sumar") {
    $id = isset($input["id"]) ? $input["id"] : "";
    $monedas = isset($input["monedas"]) ? intval($input["monedas"]) : 0;
    $empate = isset($input["empate"]) ? $input["empate"] : ""; //si no se da, es victoria

    if (!empty($id) && !empty($monedas)) {        
        $stmt = $conn->prepare("SELECT Coins, Derrotas, Victorias, Empates FROM Usuarios WHERE id = ?");
        $stmt->bind_param("s", $id);
        $stmt->execute();
        $stmt->bind_result($monedasactuales, $derrotasactuales, $victoriasactuales, $empatesactuales);
        $stmt->fetch();
        $stmt->close();

        if($monedasactuales !== null){
            $newcoins = $monedasactuales+$monedas;
            $newderrotas = $derrotasactuales-1;
            if(!empty($empate)){
                $newvictorias = $victoriasactuales;
                $newempates = $empatesactuales+1;
            }else{
                $newvictorias = $victoriasactuales+1;
                $newempates = $empatesactuales;
            }

            $stmt = $conn->prepare("UPDATE Usuarios SET Coins=?, Derrotas=?, Victorias=?, Empates=? WHERE id= ?");
            $stmt->bind_param("iiiii", $newcoins, $newderrotas, $newvictorias, $newempates, $id);
            
            if ($stmt->execute()) {
                echo json_encode(["status" => "success", "code" => "0", "message" => "Monedas agregadas correctamente", "monedas" => $newcoins]);
            } else {
                echo json_encode(["status" => "error", "code" => "1", "message" => "Error al sumar monedas."]);
            }
            $stmt->close();
        }else{
            echo json_encode(["status" => "error", "code" => "2", "message" => "Error al sumar monedas."]);
        }

    } else {
        echo json_encode(["status" => "error", "code" => "4", "message" => "Datos incompletos"]);
    }
//ruta restar monedas
}else if ($_SERVER["REQUEST_METHOD"] === "POST" && isset($input["accion"]) && $input["accion"] === "restar") {
    $id = isset($input["id"]) ? $input["id"] : "";
    $monedas = isset($input["monedas"]) ? intval($input["monedas"]) : 0;

    if (!empty($id) && !empty($monedas)) {        
        $stmt = $conn->prepare("SELECT Coins, Derrotas FROM Usuarios WHERE id = ?");
        $stmt->bind_param("s", $id);
        $stmt->execute();
        $stmt->bind_result($monedasactuales, $derrotas);
        $stmt->fetch();
        $stmt->close();

        if($monedasactuales !== null){
            $newcoins = $monedasactuales-$monedas;
            $newderrotas = $derrotas+1;
            $stmt = $conn->prepare("UPDATE Usuarios SET Coins=?, Derrotas=? WHERE id= ?");
            $stmt->bind_param("iii",$newcoins,$newderrotas,$id);

            if ($stmt->execute()) {
                echo json_encode(["status" => "success", "code" => "0", "message" => "Monedas restadas correctamente", "monedas" => $newcoins]);
            } else {
                echo json_encode(["status" => "error", "code" => "1", "message" => "Error al restar monedas."]);
            }
            $stmt->close();
        }else{
            echo json_encode(["status" => "error", "code" => "2", "message" => "Error al restar monedas."]);
        }
        
    } else {
        echo json_encode(["status" => "error", "code" => "4", "message" => "Datos incompletos"]);
    }
//ruta obtener monedas
}else if ($_SERVER["REQUEST_METHOD"] === "POST" && isset($input["accion"]) && $input["accion"] === "monedas"){
    $usuario = isset($input["usuario"]) ? $input["usuario"] : "";
    if (!empty($usuario)) {
        $stmt = $conn->prepare("SELECT Nombre,Coins FROM Usuarios WHERE nombre = ?");
        $stmt->bind_param("s", $usuario);
        $stmt->execute();
        $stmt->bind_result($nom, $coins);
        $stmt->fetch();
        $stmt->close();

        echo json_encode(["status" => "success", "code" => "0", "message" => "Monedas de jugador obtenidas", "nombre" => $nom, "monedas" => $coins]);
    }else{
        echo json_encode(["status" => "error", "code" => "1", "message" => "Datos insuficientes."]);
    }
}else{
    echo json_encode(["status" => "error", "code" => "-1", "message" => "Buscas algo?"]);
}

$conn->close();
?>
