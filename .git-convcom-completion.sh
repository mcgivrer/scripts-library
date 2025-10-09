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
