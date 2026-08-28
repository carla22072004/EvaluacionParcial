# Cuestionario — Parte A del examen de la Unidad IV

> **Cómo se llena este archivo.** Responda **dentro de este mismo archivo**, debajo de cada pregunta, en el bloque marcado como `**Respuesta:**`. No borre ni reescriba los enunciados: el evaluador compara pregunta por pregunta. No añada ni quite secciones.
>
> **Este archivo se versiona en el repositorio.** Debe existir en la raíz, llamarse exactamente `Cuestionario.md`, y sus respuestas deben llegar por *commits* sucesivos hechos cuando el docente lo indique. Un archivo que aparece completo en un único *commit* al final de la sesión no cumple el protocolo y se trata según el criterio de piso 4 del examen.
>
> Se valora la precisión técnica y la justificación, **no la extensión**. Una respuesta correcta de seis líneas vale más que una página imprecisa. Cuando la pregunta pida referirse al proyecto base, hágalo con nombres concretos de clases o de *endpoints*.

---

## Datos del estudiante

| Campo | Valor |
|---|---|
| Apellidos y nombres | |
| Número de carnet | |
| Correo institucional | |
| Fecha | |
| URL del repositorio | |

---

## A1. Restricciones de REST aplicadas a un caso concreto — 8 puntos

**a) Enuncie las seis restricciones del estilo arquitectónico REST según Fielding. (3 puntos)**

**Respuesta:**
Las seis restricciones del estilo arquitectónico REST según Roy Fielding son:
1. Cliente-servidor (Client-Server)
2. Sin estado (Stateless)
3. Cacheable (Cacheable)
4. Sistema de capas (Layered System)
5. Interfaz uniforme (Uniform Interface)
6. Código a demanda (Code on Demand)

**b) El proyecto base expone `GET /api/v1/autores` y guarda el estado de la sesión del usuario solo en el JWT que el cliente envía en cada petición. Explique qué restricción concreta se está cumpliendo con esa decisión y qué consecuencia práctica tiene para escalar el sistema a varios servidores detrás de un balanceador. (3 puntos)**

**Respuesta:**
Se está cumpliendo la restricción **Sin estado (Stateless)**, ya que el servidor no guarda información de la sesión en memoria entre peticiones. 
La consecuencia práctica es que facilita la escalabilidad horizontal: como no hay una sesión vinculada a un servidor específico, cualquier servidor detrás del balanceador de carga puede atender cualquier petición del cliente usando únicamente la información provista en el JWT.

**c) De las seis restricciones, indique cuál es opcional y dé un ejemplo real de una API que la use. (2 puntos)**

**Respuesta:**
La restricción opcional es el **Código a demanda (Code on Demand)**.
Un ejemplo real de esto es cuando una API envía un fragmento de JavaScript ejecutable al cliente para ampliar su funcionalidad en tiempo de ejecución, por ejemplo, el script de Google Analytics o un widget de soporte técnico como Intercom que el navegador descarga y ejecuta.

---

## A2. Anatomía y ciclo de vida de un JWT — 8 puntos

**a) Un JWT tiene tres partes separadas por puntos. Nómbrelas en orden e indique qué contiene cada una. (3 puntos)**

**Respuesta:**
1. **Header (Encabezado):** Contiene metadatos sobre el token, principalmente el tipo de token (JWT) y el algoritmo criptográfico utilizado para la firma (ej. HS256).
2. **Payload (Cuerpo):** Contiene los *claims* o afirmaciones, que son los datos útiles transmitidos sobre la entidad (como el ID del usuario, sus roles y tiempo de expiración).
3. **Signature (Firma):** Se genera combinando el Header y Payload codificados junto con un secreto para verificar que el token no ha sido alterado.

**b) Un compañero afirma: «como el JWT va firmado, puedo guardar en el *payload* la contraseña del usuario sin riesgo». Explique por qué está equivocado, precisando la diferencia entre firmar y cifrar. (2 puntos)**

**Respuesta:**
Está equivocado porque **firmar no es cifrar**. Firmar el token sólo garantiza su integridad (que el contenido no fue alterado), pero el *payload* está simplemente codificado en Base64Url. Esto significa que cualquier persona que intercepte el token puede decodificarlo y leer su contenido en texto claro. Guardar la contraseña allí la expondría de forma insegura.

**c) El JWT es *stateless* por diseño, lo que genera un problema conocido: no se puede invalidar un token antes de que expire. Describa dos estrategias distintas para revocarlo y señale la desventaja de cada una. (3 puntos)**

**Respuesta:**
1. **Lista negra (Blacklist) en base de datos o caché:** Consiste en guardar los identificadores de los tokens revocados hasta que expiren. *Desventaja:* Rompe la pureza de la arquitectura *stateless*, obligando al servidor a consultar este almacenamiento externo en cada petición.
2. **Tiempo de vida muy corto con Refresh Tokens:** El JWT de acceso caduca rápidamente (ej. 5 min), y se usa un Refresh Token opaco para obtener uno nuevo. *Desventaja:* Añade complejidad al cliente y aumenta la carga de peticiones al servidor para renovar tokens constantemente.

---

## A3. SOAP frente a REST — 8 puntos

**a) Complete la tabla comparativa con seis criterios entre SOAP y REST. (5 puntos)**

**Respuesta:**

| Criterio | SOAP | REST |
|---|---|---|
| Formato del mensaje | Exclusivamente XML | Típicamente JSON, pero admite XML, HTML, texto plano, etc. |
| Contrato de descripción | WSDL | OpenAPI / Swagger (opcional, no nativo al protocolo) |
| Sobrecarga de serialización | Alta (es un formato verboso y pesado por sus etiquetas SOAP Envelope/Body) | Baja (el JSON es más ligero y eficiente de procesar) |
| Tipado | Estricto y riguroso (mediante XSD) | Débil o no estructurado por defecto (depende del lenguaje/schema externo) |
| Facilidad de consumo desde un cliente móvil | Baja (requiere librerías pesadas para parsear el XML y procesar fallos) | Alta (soporte nativo para parseo de JSON en plataformas web y móviles) |
| Manejo de errores | Mediante el uso del elemento `<Fault>` dentro del cuerpo XML | Usando los códigos de estado semánticos del estándar HTTP (ej. 404, 500) |

**b) El Servicio de Rentas Internas del Ecuador expone la autorización de comprobantes electrónicos mediante servicios SOAP. Explique dos razones técnicas por las que una institución de ese tipo mantiene SOAP en lugar de migrar a REST. (3 puntos)**

**Respuesta:**
1. **Seguridad y transaccionalidad robustas de forma nativa:** SOAP cuenta con la extensión WS-Security, que permite cifrado y firmas a nivel de mensaje (no solo del transporte como TLS), lo cual es crítico para instituciones que manejan transacciones legales o financieras.
2. **Contratos fuertemente tipados:** Mediante su WSDL, SOAP garantiza que los clientes envíen información con la estructura exacta y validada previamente, minimizando fallos por datos malformados en sus integraciones de alta formalidad.

---

## A4. Cache-aside sobre un servicio externo — 8 puntos

> El proyecto base define en `CacheConfig` dos espacios de caché: `libros` con TTL de 2 minutos y `openlibrary` con TTL de 24 horas.

**a) Describa el patrón *cache-aside* en sus cuatro pasos, desde que llega la petición hasta que se responde. (3 puntos)**

**Respuesta:**
1. La aplicación intercepta la petición del cliente y consulta si la información solicitada existe en la caché utilizando su clave.
2. Si el dato se encuentra (*Cache Hit*), la aplicación lo devuelve inmediatamente al cliente sin consultar al origen subyacente.
3. Si el dato no se encuentra (*Cache Miss*), la aplicación consulta la fuente original de la verdad (la base de datos local o la API externa).
4. La aplicación toma la respuesta de esa fuente original, la guarda en la caché bajo la clave correspondiente y finalmente la retorna al cliente.

**b) Justifique técnicamente por qué el TTL de `openlibrary` es doce veces mayor que el de `libros`, y qué criterio general debe guiar la elección de un TTL. (3 puntos)**

**Respuesta:**
Técnicamente, el TTL de `openlibrary` es mayor porque representa datos estáticos que raramente cambian (como el título o el autor asociados a un ISBN internacional), por lo que se benefician de estar en caché más tiempo, evitando múltiples llamadas costosas por red hacia el servicio externo.
El criterio general para elegir un TTL debe ser el equilibrio entre la volatilidad de los datos (frecuencia con la que cambian en su origen) y la tolerancia de la aplicación a trabajar con información temporalmente desactualizada (*stale data*).

**c) Explique por qué nunca debe almacenarse en caché la respuesta de un fallo del servicio externo, y describa qué le ocurriría al sistema si se hiciera. (2 puntos)**

**Respuesta:**
Nunca debe cachearse un fallo de un servicio externo porque estaríamos provocando un "envenenamiento de caché" (*cache poisoning*). 
Si se hiciera, aunque el servicio se recupere e intente operar normalmente a los pocos segundos, nuestra aplicación seguirá devolviendo el fallo almacenado en caché a todos los usuarios hasta que el TTL expire, bloqueando el sistema de manera innecesaria.

---

## A5. Diagnóstico de códigos de estado y contrato de errores — 8 puntos

> Todos los errores del proyecto base salen en formato *Problem Details* conforme a la RFC 9457, que obsoleta a la RFC 7807.

Para cada escenario indique el código HTTP correcto y explique en una línea por qué. **Cada fila vale 1 punto** (0,5 por el código y 0,5 por la justificación); el literal g) vale 2 puntos.

| # | Escenario | Código | Justificación (una línea) |
|---|---|---|---|
| a | `GET /api/v1/libros/999999` y ese identificador no existe | **404** | El recurso solicitado mediante la URI específica no fue encontrado en el servidor. |
| b | `POST /api/v1/libros` sin cabecera `Authorization` | **401** | La solicitud carece de credenciales de autenticación válidas o faltan por completo. |
| c | Usuario autenticado con rol `LECTOR` envía `POST /api/v1/libros` | **403** | El cliente está autenticado, pero no tiene los privilegios necesarios para realizar esta acción. |
| d | `POST /api/v1/libros` con el campo `titulo` vacío | **400** | La petición del cliente está malformada o contiene errores de validación de negocio. |
| e | Prestar un libro a un socio que ya tiene tres préstamos activos | **409** | Hay un conflicto debido a una regla de negocio que choca con el estado actual del recurso. |
| f | La API de Open Library no responde dentro del *timeout* configurado | **504** | El servidor actuó como *gateway* y no obtuvo respuesta a tiempo del servicio externo. |

**g) Explique por qué devolver `200 OK` con un cuerpo `{"success": false}` es un error de diseño, y qué restricción de REST se incumple al hacerlo. (2 puntos)**

**Respuesta:**
Es un error de diseño porque enmascara una operación fallida detrás de un estado de éxito HTTP, obligando al cliente a analizar el cuerpo JSON para descubrir el error real.
Esto incumple la restricción de **Interfaz Uniforme** (específicamente la de usar *mensajes auto-descriptivos*), ya que REST dicta que la semántica del fallo se debe expresar nativa y primeramente a través del protocolo HTTP, usando los códigos de estado apropiados (ej. 4xx o 5xx).

---

## Declaración de honestidad académica

Marque con una `x` y complete:

- [x] Declaro que estas respuestas son de mi autoría, redactadas durante la sesión de examen, sin asistencia de inteligencia artificial ni comunicación con terceros.

Firma (nombre completo): Carla Zamora Arias
