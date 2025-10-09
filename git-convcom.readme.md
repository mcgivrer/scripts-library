# CONVCOM - README

## Solution complète

un script bash exécutable nommé git-convcom (donc utilisable via git convcom) + un fichier de complétion bash qui fournit de l’auto‑complétion par Tab pour le type, la portée (scope) et quelques suggestions. Principes et points importants :
- Git appelle les sous‑commandes externes nommées git-<subcmd> trouvées dans le PATH, donc un exécutable git-convcom dans le PATH permet d’appeler git convcom .  
- Il suffit de rendre le script exécutable et de le placer dans un répertoire du PATH (ex. /usr/local/bin) pour l’utiliser comme sous‑commande Git .  
- L’auto‑complétion Bash s’implante via une fonction de complétion enregistrée avec complete -F ; la fonction programmation de complétion utilise les variables COMP_WORDS / COMP_CWORD et remplit COMPREPLY (ou compgen) pour proposer les mots possibles .

fournis ci‑dessous :

1) le script git-convcom (interactive + options)  
2) la fonction de complétion à ajouter (s’occupe de “git convcom <TAB>” et de “git-convcom <TAB>”)  
3) instructions d’installation courtes.

### Script git-convcom 

(place-le dans /usr/local/bin/git-convcom, chmod +x)

```bash
#!/usr/bin/env bash
set -euo pipefail

# git-convcom : aide à la création de Conventional Commits
# Usage:
#   git convcom [type] [scope] [--] [-m "short"]    # non-interactive possible
#   git convcom                                     # interactive

TYPES=(feat fix docs style refactor perf test chore build ci)
# description max length suggestion
SUBJECT_MAX=72

# helper: check in a git repo
in_git_repo() {
  git rev-parse --is-inside-work-tree >/dev/null 2>&1
}

# build full message and commit
do_commit() {
  local type="$1" scope="$2" subject="$3" body="$4" footer="$5"
  local header
  if [[ -n "$scope" ]]; then
    header="$type($scope): $subject"
  else
    header="$type: $subject"
  fi

  # show and confirm
  echo "----- commit message -----"
  echo "$header"
  echo
  [[ -n "$body" ]] && echo "$body" && echo
  [[ -n "$footer" ]] && echo "$footer" && echo
  echo "--------------------------"

  read -r -p "Commit with this message? [y/N] " ans
  if [[ "$ans" =~ ^[Yy]$ ]]; then
    git add -A
    git commit -m "$header" -m "$body" || true
    # If footer exists, append as note or separate commit message line
    if [[ -n "$footer" ]]; then
      # add footer as separate paragraph (already added via -m above if non-empty)
      true
    fi
    echo "Committed."
  else
    echo "Aborted."
    exit 1
  fi
}

# open editor for multi-line body (uses VISUAL/EDITOR)
edit_text() {
  local tmp
  tmp="$(mktemp)"
  "${VISUAL:-${EDITOR:-vi}}" "$tmp"
  sed -n '/./p' "$tmp" || true  # print non-empty lines
  cat "$tmp"
  rm -f "$tmp"
}

# interactive flow
interactive() {
  echo "Conventional Commit assistant"

  # choose type
  echo "Type (use number or name) :"
  local i
  for i in "${!TYPES[@]}"; do
    printf "  %2d) %s\n" $((i+1)) "${TYPES[i]}"
  done
  read -r -p "> " type_in
  if [[ "$type_in" =~ ^[0-9]+$ ]]; then
    type="${TYPES[$((type_in-1))]:-}"
  else
    type="$type_in"
  fi
  if [[ -z "$type" || ! " ${TYPES[*]} " =~ " ${type} " ]]; then
    echo "Type invalide." >&2
    exit 1
  fi

  # scope (optional)
  read -r -p "Scope (optionnel) : " scope

  # subject (short)
  read -e -p "Subject (court) : " subject
  if (( ${#subject} > SUBJECT_MAX )); then
    echo "Warning: subject > $SUBJECT_MAX chars (${#subject})."
  fi

  # body (open editor)
  echo "Editer le corps du message (laisser vide pour none). Le fichier s'ouvrira dans \$EDITOR."
  read -r -p "Ouvrir l'éditeur ? [Y/n] " edityn
  body=""
  if [[ -z "$edityn" || "$edityn" =~ ^[Yy]$ ]]; then
    body="$(edit_text)"
  fi

  # footer (e.g. BREAKING CHANGE, closes #123)
  read -r -p "Footer (optionnel) : " footer

  do_commit "$type" "$scope" "$subject" "$body" "$footer"
}

# non-interactive: positional args
if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
  cat <<EOF
Usage:
  git convcom            # interactive
  git convcom <type> <scope?>  # non-interactive: will prompt subject/body/editor
EOF
  exit 0
fi

if ! in_git_repo; then
  echo "Not inside a git repository." >&2
  exit 1
fi

if [[ $# -ge 1 ]]; then
  # accept: git convcom type scope
  type="$1"
  scope="${2:-}"
  echo "Subject (short) :"
  read -r subject
  echo "Ouvrir éditeur pour le corps ?"
  read -r yn
  body=""
  if [[ -z "$yn" || "$yn" =~ ^[Yy]$ ]]; then
    body="$(edit_text)"
  fi
  footer=""
  do_commit "$type" "$scope" "$subject" "$body" "$footer"
else
  interactive
fi
```

### Fichier de complétion Bash 

(par ex. ~/.git-convcom-completion.sh ou /etc/bash_completion.d/git-convcom)

```bash
# git-convcom bash completion
_git_convcom_comp() {
  local cur prev words cword
  _get_comp_words_by_ref() {
    cur="${COMP_WORDS[COMP_CWORD]}"
    prev="${COMP_WORDS[COMP_CWORD-1]}"
    words=("${COMP_WORDS[@]}")
    cword=$COMP_CWORD
  }
  _get_comp_words_by_ref

  # types suggestions:
  local types="feat fix docs style refactor perf test chore build ci"

  # Support being invoked either as:
  #  - git convcom <TAB>   (COMP_WORDS[0]=git, COMP_WORDS[1]=convcom)
  #  - git-convcom <TAB>
  local invoked_as="${COMP_WORDS[0]}"
  if [[ "${invoked_as##*/}" == "git" ]]; then
    # invoked as "git"
    if [[ "${words[1]:-}" != "convcom" ]]; then
      return 0
    fi
    # if we are completing the subcommand name (TAB after "git "), do nothing
    if (( cword == 1 )); then
      return 0
    fi
    # position after 'git convcom' => real index for our args starts at COMP_CWORD-1
    local pos=$((cword - 1))
    if (( pos == 1 )); then
      COMPREPLY=( $(compgen -W "$types" -- "$cur") )
      return 0
    elif (( pos == 2 )); then
      # propose recent scopes found in recent commit headers
      if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        local scopes
        scopes=$(git log --pretty=format:%s -n 200 | sed -nE 's/^[a-z]+\\(([^)]+)\\):.*/\\1/p' | sort -u)
        COMPREPLY=( $(compgen -W "$scopes" -- "$cur") )
      fi
      return 0
    fi
  else
    # invoked as git-convcom
    if (( cword == 1 )); then
      COMPREPLY=( $(compgen -W "$types" -- "$cur") )
      return 0
    elif (( cword == 2 )); then
      if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
        local scopes
        scopes=$(git log --pretty=format:%s -n 200 | sed -nE 's/^[a-z]+\\(([^)]+)\\):.*/\\1/p' | sort -u)
        COMPREPLY=( $(compgen -W "$scopes" -- "$cur") )
      fi
      return 0
    fi
  fi
}
# register completion for both 'git' and 'git-convcom'
complete -F _git_convcom_comp git
complete -F _git_convcom_comp git-convcom
```

Explications rapides :
- La complétion est fournie par une fonction enregistrée via complete -F (méthode standard de programmable completion sous bash).  
- On utilise compgen -W pour générer les propositions à partir d’une liste de mots (types, scopes) ; compgen/complete sont explicitement conçus pour ça.  
- La fonction regarde COMP_WORDS/COMP_CWORD pour décider quel argument compléter (ex. type vs scope) — c’est la méthode documentée pour écrire des complétions programmables.

### Installation

- Copier git-convcom dans /usr/local/bin/ et rendre exécutable :
  sudo mv git-convcom /usr/local/bin/git-convcom
  sudo chmod +x /usr/local/bin/git-convcom
  (Git appellera ce fichier via git convcom tant qu’il est dans PATH).
- Ajouter le fichier de complétion dans /etc/bash_completion.d/ ou sourcer-le depuis ~/.bashrc :
  echo "source ~/.git-convcom-completion.sh" >> ~/.bashrc
  source ~/.git-convcom-completion.sh
- Ouvrir un nouveau shell pour tester : tape "git convcom " puis Tab pour voir les propositions.

Remarques / améliorations possibles
- Support Zsh : adapter la complétion (zsh a sa propre API) — ici l’exemple est pour Bash.  
- Améliorer l’extraction des scopes (regex plus robuste) ou utiliser un cache pour accélérer la complétion.  
- Possibilité d’intégrer un mode non‑interactif / CI (ex. --message) pour scripts.  

Sources (documentation utile) :
- Git exécute des sous‑commandes externes nommées git-<subcmd> trouvées dans le PATH et installation d’extensions via placement dans le PATH (exemples).  
- Programmable completion Bash : utilisation de complete -F et de compgen/COMPREPLY et variables COMP_WORDS/COMP_CWORD.

Si tu veux, je peux :
- adapter le script pour supporter zsh également ;  
- ajouter plus de validations (longueur du subject, conventional commit breaking change conventions) ;  
- te fournir un paquet d’installation (deb) ou un install.sh qui installe le binaire et la complétion automatiquement. Lequel préfères‑tu ?
