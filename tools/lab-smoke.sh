#!/usr/bin/env bash
# Graphical smoke test of a lab of the kit (org.atriasoft.ege.lab): runs it in its own virtual X server (Xvfb), so
# it never takes the mouse or the keyboard of the real session, sends keys and clicks, and takes screenshots at
# given times. The only way an automated session may open a lab. Each lab has a thin wrapper that names its
# launcher (./eFlora/tools/lab-smoke.sh, ./eArchi/tools/lab-smoke.sh).
#
#   ./ege/tools/lab-smoke.sh LAB_SCRIPT OUTDIR "LAB ARGS" STEP...
#
# LAB_SCRIPT is the launcher of the lab (it compiles with LAB_COMPILE_ONLY=1 and no display, then runs the lab with
# the arguments; LAB_JAVA_OPTS, LAB_MAIN and LAB_CLASSPATH are passed to it).
# Each STEP is TIME:ACTION, TIME in seconds since the launch (decimals allowed):
#   8:shot                screenshot OUTDIR/shot_8.png       8:shot=name  screenshot OUTDIR/name.png
#   9:key=h               press and release a key (xdotool names: h, F5, Tab, Escape, Page_Up...)
#   9:keydown=Left  11:keyup=Left   hold a key
#   9:click=1             click a mouse button: 1 left, 3 right; 4 the wheel up (away from you: the 3D view comes
#                         closer, the panel scrolls up), 5 the wheel down (farther, the panel scrolls down);
#                         mousedown= / mouseup= to hold one; 9:dblclick=1 a double click (the splitter between
#                         the view and the panel: back to the default width)
#   9:moveto=1100,300     move the pointer to a point of the window (the panel of controls is on the right)
#   9:move=40,-10         move the pointer by so many pixels (a drag between mousedown and mouseup)
#   9:run=COMMAND         run a shell command (break or mend a data file given with --data)
#   9:close=1220,667      click the Quit button there (in the footer of the panel, always in sight: 1220,667 at
#                         the default size): the lab must end with code 0 within 10 s (the last step)
#   9:quit                stop (also done after the last step)
#
# The lab runs with its own user folder (OUTDIR/home), its lab settings cleared first: the panel starts at its
# default width (Quit at 1220,667); SMOKE_KEEP_SETTINGS=1 keeps those of the run before (the width of the panel
# dragged then). SMOKE_SIZE=1280x760 sets the size of the virtual screen. Output of the lab: OUTDIR/lab.log.
# Exit status: 0 when the lab still ran at the end (or ended with code 0 after close=) and no exception was
# logged, 1 otherwise; 2 when it cannot run.
#
#   ./ege/tools/lab-smoke.sh eFlora/tools/lab.sh eFlora/out/lab "" 10:shot=oak 11:key=h 13:shot=proxies
set -u

if [ $# -lt 3 ]; then
	sed -n '2,32p' "$0" | sed 's/^# \{0,1\}//'
	exit 2
fi
for tool in Xvfb xdotool import; do
	command -v "$tool" > /dev/null || { echo "lab-smoke: '$tool' is not installed" >&2; exit 2; }
done

LAB_SCRIPT=$1
OUT=$2
ARGS=$3
shift 3
[ -x "$LAB_SCRIPT" ] || { echo "lab-smoke: '$LAB_SCRIPT' is not an executable launcher" >&2; exit 2; }
LAB_SCRIPT=$(cd "$(dirname "$LAB_SCRIPT")" && pwd)/$(basename "$LAB_SCRIPT")
mkdir -p "$OUT"
OUT=$(cd "$OUT" && pwd)
mkdir -p "$OUT/home"
[ "${SMOKE_KEEP_SETTINGS:-0}" = 1 ] || rm -rf "$OUT/home/.config/atriasoft/lab"
SIZE=${SMOKE_SIZE:-1280x760}

# Compiled before the clock starts (the steps count from the launch of the window): nothing is run, and without
# any display, so that no window can open on the real screen.
env -u DISPLAY -u WAYLAND_DISPLAY LAB_COMPILE_ONLY=1 "$LAB_SCRIPT" \
	|| { echo "lab-smoke: the lab does not build" >&2; exit 2; }

# Xvfb picks a free display itself and writes its number: safe for parallel runs.
DISPLAY_FILE=$(mktemp)
Xvfb -displayfd 3 -screen 0 "${SIZE}x24" -ac +extension GLX +render -noreset 3> "$DISPLAY_FILE" 2> "$OUT/xvfb.log" &
XVFB_PID=$!
LAB_PID=
cleanup() {
	[ -n "$LAB_PID" ] && kill "$LAB_PID" 2> /dev/null && sleep 1 && kill -9 "$LAB_PID" 2> /dev/null
	kill "$XVFB_PID" 2> /dev/null
	rm -f "$DISPLAY_FILE"
}
trap cleanup EXIT
for _ in $(seq 50); do
	[ -s "$DISPLAY_FILE" ] && break
	sleep 0.1
done
[ -s "$DISPLAY_FILE" ] || { echo "lab-smoke: Xvfb did not start (see $OUT/xvfb.log)" >&2; exit 2; }
export DISPLAY=":$(cat "$DISPLAY_FILE")"
unset WAYLAND_DISPLAY

# shellcheck disable=SC2086
LAB_JAVA_OPTS="-Duser.home=$OUT/home" "$LAB_SCRIPT" $ARGS > "$OUT/lab.log" 2>&1 &
LAB_PID=$!
START=$(date +%s.%N)

window() {
	xdotool search --onlyvisible --pid "$LAB_PID" 2> /dev/null | tail -1
}

# Without a window manager the window keeps where it opened: brought to the corner once, focused by a click there.
placed=
place() {
	[ -n "$placed" ] && return
	local id
	id=$(window)
	[ -n "$id" ] || return
	xdotool windowmove "$id" 0 0 windowfocus "$id" 2> /dev/null
	xdotool mousemove 3 3 click 1
	placed=1
}

status=0
closed=
for step in "$@"; do
	at=${step%%:*}
	action=${step#*:}
	while kill -0 "$LAB_PID" 2> /dev/null; do
		place
		awk -v now="$(date +%s.%N)" -v start="$START" -v at="$at" 'BEGIN { exit !(now - start >= at) }' && break
		sleep 0.2
	done
	if ! kill -0 "$LAB_PID" 2> /dev/null; then
		echo "lab-smoke: the lab exited before step $step" >&2
		status=1
		break
	fi
	name=${action%%=*}
	value=
	[ "$name" != "$action" ] && value=${action#*=}
	case "$name" in
		shot)
			file="$OUT/${value:-shot_$at}.png"
			import -window root "$file" 2>> "$OUT/import.log" && echo "lab-smoke: $file"
			;;
		key) xdotool key "$value" ;;
		keydown) xdotool keydown "$value" ;;
		keyup) xdotool keyup "$value" ;;
		click) xdotool click "$value" ;;
		dblclick) xdotool click --repeat 2 --delay 80 "$value" ;;
		mousedown) xdotool mousedown "$value" ;;
		mouseup) xdotool mouseup "$value" ;;
		move) xdotool mousemove_relative -- "${value%%,*}" "${value##*,}" ;;
		moveto) xdotool mousemove "${value%%,*}" "${value##*,}" ;;
		run) sh -c "$value" ;;
		close)
			xdotool mousemove "${value%%,*}" "${value##*,}" click 1
			for _ in $(seq 50); do
				kill -0 "$LAB_PID" 2> /dev/null || break
				sleep 0.2
			done
			if kill -0 "$LAB_PID" 2> /dev/null; then
				echo "lab-smoke: the lab still runs 10 s after its Quit control" >&2
				status=1
			else
				wait "$LAB_PID"
				code=$?
				LAB_PID=
				closed=1
				echo "lab-smoke: the lab ended with code $code"
				[ "$code" -eq 0 ] || status=1
			fi
			break
			;;
		quit) break ;;
		*) echo "lab-smoke: unknown action '$action'" >&2; status=1 ;;
	esac
done

if [ -z "$closed" ] && ! kill -0 "$LAB_PID" 2> /dev/null; then
	echo "lab-smoke: the lab is no longer running at the end (crash?)" >&2
	status=1
fi
if grep -q "Exception\|Error:" "$OUT/lab.log"; then
	echo "lab-smoke: exceptions in $OUT/lab.log:" >&2
	grep -n "Exception\|Error:" "$OUT/lab.log" | head -5 >&2
	status=1
fi
exit $status
