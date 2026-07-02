# AppMapas - Guia Turistica Quevedo

Aplicacion Android desarrollada en Kotlin para la visualizacion y filtrado dinamico de lugares turisticos en la ciudad de Quevedo, utilizando Google Maps SDK y servicios web mediante la libreria Volley.

## Caracteristicas Principales

La aplicacion cumple con los siguientes requisitos tecnicos:

1. Interfaz del Mapa:
   - Integracion completa de Google Maps con controles de zoom.
   - Circulo de busqueda: Visualizacion dinamica del radio de alcance en color rojo.
   - Slider de Radio: Control de precision que permite ajustar el rango de busqueda entre 100 y 1000 metros.
   - Cambio de Capa: Boton flotante (FAB) para alternar entre vista Satelital y Normal.

2. Categorias y Subcategorias Dinamicas:
   - Peticion en tiempo real a la API para llenar la lista de Categorias.
   - Carga jerarquica de Subcategorias basada en la seleccion del usuario.
   - Inclusion de la opcion "TODOS" para una visualizacion global de puntos.

3. Visualizacion de Lugares Turisticos:
   - Marcadores personalizados en el mapa con los nombres de los establecimientos.
   - Informacion Detallada: Ventanas de informacion (InfoWindow) que muestran el nombre y la direccion o referencia extraida del servidor.
   - Cero Datos Hardcoded: Toda la informacion proviene exclusivamente de la base de datos externa.

4. Sistema de Filtros Avanzado:
   - Filtrado por URL: Envio de parametros especificos (idsubcategoria) al Web Service.
   - Filtro Logico Local: Validacion de seguridad en el codigo cliente para asegurar que solo se muestren los lugares pertenecientes a la categoria seleccionada.

## Tecnologias Utilizadas

- Lenguaje: Kotlin
- Mapas: Google Maps SDK for Android
- Networking: Volley (Implementacion moderna para gestion de peticiones HTTP).
- Formatos: JSON para la comunicacion con el servidor.

## Endpoints Utilizados (API)

La aplicacion consume los siguientes servicios desde la IP 35.153.103.86:

- Listado de Categorias: /categoria/getlistadoCB
- Listado de Subcategorias: /subcategoria/getlistadoCB/{id_categoria}
- Busqueda por Radio y Coordenadas: /lugar_turistico/json_getlistadoMapa?lat={lat}&lng={lng}&radio={radio_en_km}

## Requisitos de Instalacion

1. Clonar el repositorio.
2. Configurar una API KEY de Google Maps valida en el archivo AndroidManifest.xml.
3. Asegurarse de tener conexion a Internet para la carga de los Web Services.
4. La aplicacion inicia automaticamente en el centro de Quevedo para la exploracion de lugares.

---
Desarrollado como proyecto practico de aplicaciones moviles.
